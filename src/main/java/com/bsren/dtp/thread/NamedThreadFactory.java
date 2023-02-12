package com.bsren.dtp.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NamedThreadFactory implements ThreadFactory {

    private final ThreadGroup group;

    private final String namePrefix;

    private final boolean daemon;

    private final Integer priority;

    private final AtomicInteger seq = new AtomicInteger(1);

    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public NamedThreadFactory(String namePrefix,
                              boolean daemon,
                              int priority){
        this.daemon = daemon;
        this.priority = priority;
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
        this.uncaughtExceptionHandler = new DtpUncaughtExceptionHandler();
    }

    public NamedThreadFactory(String namePrefix){
        this(namePrefix,false,Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String namePrefix,boolean daemon){
        this(namePrefix,daemon,Thread.NORM_PRIORITY);
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = namePrefix+seq.getAndIncrement();
        Thread t = new Thread(group,r,name);
        t.setDaemon(daemon);
        t.setPriority(priority);
        t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return t;
    }


    /**
     * 在多线程环境中，执行runnable任务时，线程抛出的异常是不能用try....catch捕获的，
     * 这样就有可能导致一些问题的出现，比如异常的时候无法回收一些系统资源，或者没有关闭当前的连接等等。
     *
     */
    public static class DtpUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.error("thread {} throw exception {}", t, e);
        }
    }
}
