package com.bsren.dtp.support;

import java.util.List;
import java.util.concurrent.*;

import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.thread.DtpExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
/**
 * 在bean加载到容器后对配置进行初始化
 * 在bean销毁后根据waitForTasksToCompleteOnShutdown选择线程池shutdown或者shutdownNow，如果不立刻shutdown则取消返回的任务
 * 根据awaitTerminationSeconds选择等待的时间，如果等待过程中被中断则抛出异常
 */
@Slf4j
public abstract class DtpLifecycleSupport extends ThreadPoolExecutor implements InitializingBean,DisposableBean{

    protected String threadPoolName;

    protected boolean waitForTasksToCompleteOnShutdown = false;

    protected int awaitTerminationSeconds;

    public DtpLifecycleSupport(int corePoolSize,
                               int maximumPoolSize,
                               long keepAliveTime,
                               TimeUnit unit,
                               BlockingQueue<Runnable> workQueue,
                               ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public void setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
    }

    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationSeconds = awaitTerminationSeconds;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public boolean isWaitForTasksToCompleteOnShutdown() {
        return waitForTasksToCompleteOnShutdown;
    }

    public int getAwaitTerminationSeconds() {
        return awaitTerminationSeconds;
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DtpProperties properties = ApplicationContextHolder.getBean(DtpProperties.class);
        initialize(properties);
    }

    @Override
    public void destroy() throws Exception {
        internalShutdown();
    }

    private void internalShutdown(){
        log.info("shutting down executorService, pool name:{}",threadPoolName);
        if(this.waitForTasksToCompleteOnShutdown){
            this.shutdown();
        }else {
            List<Runnable> runnableList = this.shutdownNow();
            for (Runnable runnable : runnableList) {
                if(runnable instanceof Future){
                    Future<?> future = (Future<?>)runnable;
                    future.cancel(true);
                }
            }
        }
        awaitTerminationIfNecessary();
    }

    /**
     * 当你捕获InterruptException并吞下它时，你基本上阻止任何更高级别的方法/线程组注意到中断。这可能会导致问题。
     * 通过调用Thread.currentThread().interrupt()，你可以设置线程的中断标志，因此更高级别的中断处理程序会注意到它并且可以正确处理它。
     */
    private void awaitTerminationIfNecessary() {
        if(this.awaitTerminationSeconds<=0){
            return;
        }
        try {
            if(!awaitTermination(this.awaitTerminationSeconds, TimeUnit.SECONDS)){
                log.warn("time out while waiting for executor {} to terminate",threadPoolName);
            }
        } catch (InterruptedException e) {
            log.warn("interrupted while waiting for executor {} to terminate",threadPoolName);
            Thread.currentThread().interrupt();
        }
    }

    protected abstract void initialize(DtpProperties properties);


}
