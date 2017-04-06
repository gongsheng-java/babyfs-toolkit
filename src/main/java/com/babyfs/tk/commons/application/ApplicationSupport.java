package com.babyfs.tk.commons.application;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Module;
import com.babyfs.tk.commons.service.ILifeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * IApplication的基本实现类,负责管理所有{@link ILifeService}的启动和停止,如果一个{@link ILifeService}实例想被接管,可以使用
 * {@link LifeServiceBindUtil#addLifeService(com.google.inject.Binder, ILifeService)}注册即可
 */
public abstract class ApplicationSupport extends AbstractService implements IApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSupport.class);
    protected final List<Module> modules = Lists.newArrayList();
    protected final List<Module> overridingModules = Lists.newArrayList();
    protected String[] args;
    private final Object lock = new Object();

    /**
     * 非guice注入的LifeServcie,通过{@link #addILifeServvice(ILifeService)}添加
     */
    private final Set<ILifeService> manulaLifeServices = Sets.newHashSet();

    @Override
    protected void doStart() {
        LOGGER.info("Starting manual services");
        synchronized (lock) {
            for (ILifeService lifeService : manulaLifeServices) {
                LOGGER.info("Starting service name:{},class:{}", lifeService.getName(), lifeService.getClass());
                lifeService.startAsync().awaitRunning();
                LOGGER.info("Finish starting service name:{},class:{}", lifeService.getName(), lifeService.getClass());
            }
        }
        LOGGER.info("Starting manual service,finish");
        notifyStarted();
    }

    @Override
    protected void doStop() {
        LOGGER.info("Stoping manual service");
        synchronized (lock) {
            for (ILifeService lifeService : manulaLifeServices) {
                LOGGER.info("Stoping service name {},class:{}", lifeService.getName(), lifeService.getClass());
                lifeService.stopAsync().awaitTerminated();
                LOGGER.info("Finish stoping service name:{},class:{}", lifeService.getName(), lifeService.getClass());
            }
        }
        LOGGER.info("Stoping manual service,finish");
        notifyStopped();
    }

    @Override
    public Iterable<? extends Module> getModules() {
        return this.modules;
    }

    @Override
    public Iterable<? extends Module> getOverridingModules() {
        return this.overridingModules;
    }

    @Override
    public void addILifeServvice(ILifeService service) {
        Preconditions.checkArgument(service != null, "service");
        synchronized (lock) {
            LOGGER.info("Manual add life servcie:" + service);
            this.manulaLifeServices.add(service);
        }
    }

    @Override
    public void init(String[] args) {
        this.args = args;
    }

    @Override
    public String[] getArgs() {
        return this.args;
    }

    protected synchronized void addModule(Module module) {
        this.modules.add(module);
    }

    protected synchronized void addModules(List<Module> modules) {
        this.modules.addAll(modules);
    }
}
