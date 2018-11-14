package com.babyfs.tk.commons.application;

import com.alibaba.dubbo.remoting.transport.netty.NettyClient;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import com.babyfs.tk.commons.service.ILifeService;
import com.babyfs.tk.commons.service.IStageActionRegistry;
import com.babyfs.tk.commons.service.LifecycleModule;
import com.babyfs.tk.commons.service.annotation.AfterStartStage;
import com.babyfs.tk.commons.service.annotation.InitStage;
import com.babyfs.tk.commons.service.annotation.ShutdownStage;
import org.jboss.netty.channel.ChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 应用程序的启动器,一个应用程序的启动顺序如下:
 * step 1. {@link InitStage} 该阶段进行服务的启动初始化,例如:
 * a. RPC service进行服务绑定
 * b. 服务进行配置
 * c. 其他需要在服务真正启动前进行的操作
 * 该阶段的操作由{@link InitStage}标注的{@link IStageActionRegistry#execute()}负责统一执行,
 * 由{@link AppLauncher#run()}触发调用.
 * <p/>
 * step 2. 启动服务
 * 在step1执行完毕,并且无异常的情况下,调用{@link IApplication#startAsync()} 启动App
 * 例如{@link ApplicationSupport}在start时会依次调用所有已经注册到他的服务的{@link ILifeService#startAsync()}
 * <p/>
 * step 3. 运行中
 * step2完成后,进入此状态
 * <p/>
 * setp 4. {@link ShutdownStage} 停止阶段
 * Guice 在inject的时候,会通过{@link LifecycleModule.StageInit}注册shutdownHook,这样
 * 当jvm退出时,会触发通过{@link ShutdownStage} 注册的所有动作的执行:
 * a.  {@link IApplication#stopAsync()},由{@link AppLauncerModule}注册
 * b. 其他模块自己注册的动作
 * <p/>
 */
public final class AppLauncher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppLauncher.class);

    private static final String DEFAULT_NOT_DAEMON = "Hashed wheel timer";

    private String[] args;

    @Inject
    @InitStage
    private IStageActionRegistry startActionRegistry;

    @Inject
    @AfterStartStage
    private IStageActionRegistry afterStartActionRegistry;

    /**
     * @param args args[0] {@link IApplication}的实现类名
     *             args[1:] args[0]的参数
     */
    public AppLauncher(String[] args) {
        Preconditions.checkArgument(args != null && args.length > 0, "args must not be null or empty");
        this.args = args;
    }

    /**
     * 执行应用启动
     */
    public void run() {
        final Stage stage = Stage.PRODUCTION;
        final IApplication application = getApplication();
        String[] applicationArgs = new String[args.length - 1];
        if (this.args.length > 1) {
            System.arraycopy(this.args, 1, applicationArgs, 0, applicationArgs.length);
        }
        application.init(applicationArgs);
        Iterable<Module> modules = ImmutableList.<Module>builder().add(new AppLauncerModule(application)).build();
        try {
            Injector injector = Guice.createInjector(stage, Modules.override(Modules.combine(modules)).with(application.getOverridingModules()));
            LifecycleModule.initServiceContext(injector);
            injector.injectMembers(this);

            LOGGER.info("Starting stage action ");
            this.startActionRegistry.execute();
            LOGGER.info("Finished stage action");

            LOGGER.info("Starting application:" + application.getClass().getName());
            application.startAsync().awaitRunning();
            LOGGER.info("Finished application:" + application.getClass().getName());

            LOGGER.info("Starting after start stage action ");
            this.afterStartActionRegistry.execute();
            LOGGER.info("Finished after start stage action,finish");
        } catch (Exception e) {
            LOGGER.error("Staring application fail,exit(1)", e);
            System.exit(1);
        }
    }

    /**
     * 解析Application
     *
     * @return
     */
    private IApplication getApplication() {
        String appClassName = this.args[0];
        Preconditions.checkState(!Strings.isNullOrEmpty(appClassName), "Can't find the argument app_class");
        IApplication application = null;
        try {
            application = (IApplication) Class.forName(appClassName).newInstance();
        } catch (Exception e) {
            LOGGER.error("Parse application fail for [" + appClassName + "]", e);
            throw new RuntimeException(e);
        }
        return application;
    }

    public static void main(String[] args) {
        AppLauncher appLauncher = new AppLauncher(args);
        try {
            appLauncher.run();
        } catch (Exception e) {
            LOGGER.error("Failed to start application,exit 1", e);
            System.exit(1);
        }

        tryToStop();//释放netty 资源
    }

    private static void tryToStop(){
        Executors.newFixedThreadPool(1, r -> {
            Thread th = new Thread(r);
            th.setDaemon(true);
            th.setName("netty resource watcher");
            return th;
        }).execute(() -> {
            for(;1==1;){
                Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
                boolean canStop = true;
                for (Map.Entry<Thread, StackTraceElement[]> entry:
                        allStackTraces.entrySet()) {
                    Thread th = entry.getKey();
                    if(entry.getKey().equals(Thread.currentThread())){
                        continue;
                    }

                    String threadName = th.getName();
                    if(th.isDaemon() || (threadName != null && threadName.contains(DEFAULT_NOT_DAEMON))){
                        continue;
                    }
                    LOGGER.info("there is non-daemon thread left! cannot release Netty, thread name is {}", threadName);
                    canStop = false;
                    break;
                }

                if(canStop){
                    releaseNettyClientExternalResources();
                    break;
                }

                try {
                    Thread.sleep(10000);//每10秒扫描是否有必要结束dubbo netty
                } catch (InterruptedException e) {
                }
            }
        });
    }

    private static void releaseNettyClientExternalResources() {
        try {
            Field field = NettyClient.class.getDeclaredField("channelFactory");
            field.setAccessible(true);
            ChannelFactory channelFactory = (ChannelFactory) field.get(NettyClient.class);
            channelFactory.releaseExternalResources();
            field.setAccessible(false);
            LOGGER.info("Release NettyClient's external resources");
        } catch (Exception e){
            LOGGER.error("Release NettyClient's external resources error", e);
        }
    }
}
