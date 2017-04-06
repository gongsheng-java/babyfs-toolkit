package com.babyfs.tk.commons.application;

import com.babyfs.tk.commons.service.LifeServiceSupport;
import com.babyfs.tk.commons.thread.NamedThreadFactory;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * 简单的命令行服务,接受从命令行传入的参数,在一个独立的线程中并执行{@link #main()}
 * 命令行参数有以下途径注入:
 * <ul>
 * <li>
 * 由{@link AppLauncerModule}通过{@link IApplication#BIND_APP_ARGS_NAME}注入
 * </li>
 * <li>
 * 使用{@link #setArgs(String[])}直接注入
 * </li>
 * </ul>
 */
public abstract class SimpleConsoleService extends LifeServiceSupport {
    private static final NamedThreadFactory THREAD_FACTORY = new NamedThreadFactory("console", false);
    private Thread thread;

    @Inject
    @Named(IApplication.BIND_APP_ARGS_NAME)
    private String[] args;

    public SimpleConsoleService() {
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }


    @Override
    protected synchronized void execStart() {
        super.execStart();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                SimpleConsoleService.this.main();
            }
        };
        thread = THREAD_FACTORY.newThread(task);
        thread.start();
    }

    @Override
    protected synchronized void execStop() {
        super.execStop();
        if (this.thread != null) {
            if (this.thread.isAlive()) {
                this.thread.interrupt();
            }
            this.thread = null;
        }
    }

    public abstract void main();
}
