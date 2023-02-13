package com.bsren.dtp.thread;

import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.reject.RejectHandlerGetter;
import com.bsren.dtp.runnable.DtpRunnable;
import com.bsren.dtp.support.DtpLifecycleSupport;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class DtpExecutor extends DtpLifecycleSupport {

    /**
     * 拒绝策略
     */
    private String rejectHandlerName;

    /**
     * 别名
     */
    private String threadPoolAliasName;

    /**
     * 通知项
     */
    private List<NotifyItem> notifyItems;

    public List<NotifyItem> getNotifyItems() {
        return notifyItems;
    }

    public boolean isNotifyEnabled() {
        return notifyEnabled;
    }

    public boolean isPreStartAllCoreThreads() {
        return preStartAllCoreThreads;
    }

    public long getRunTimeout() {
        return runTimeout;
    }

    public long getQueueTimeout() {
        return queueTimeout;
    }

    public LongAdder getRunTimeoutCount() {
        return runTimeoutCount;
    }

    public LongAdder getQueueTimeoutCount() {
        return queueTimeoutCount;
    }

    /**
     * 是否开启通知
     */
    private boolean notifyEnabled;


    /**
     * 是否在线程池创建的时候直接创建核心线程
     */
    private boolean preStartAllCoreThreads;

    /**
     * 执行超时的时间
     */
    private long runTimeout;

    /**
     * 排队超时的时间
     */
    private long queueTimeout;

    /**
     * 拒绝的次数
     */
    private final LongAdder rejectCount = new LongAdder();


    private int runTimeoutThreshold = 1;

    private int queueTimeoutThreshold = 1;

    private int rejectThreshold = 1;

    public int getQueueTimeoutThreshold() {
        return queueTimeoutThreshold;
    }

    public void setQueueTimeoutThreshold(int queueTimeoutThreshold) {
        this.queueTimeoutThreshold = queueTimeoutThreshold;
    }

    public int getRunTimeoutThreshold() {
        return runTimeoutThreshold;
    }

    public void setRunTimeoutThreshold(int runTimeoutThreshold) {
        this.runTimeoutThreshold = runTimeoutThreshold;
    }

    public int getRejectThreshold() {
        return rejectThreshold;
    }

    public void setRejectThreshold(int rejectThreshold) {
        this.rejectThreshold = rejectThreshold;
    }

    public void setRunTimeout(long runTimeout) {
        this.runTimeout = runTimeout;
    }

    public void setQueueTimeout(long queueTimeout) {
        this.queueTimeout = queueTimeout;
    }

    public String getThreadPoolAliasName() {
        return threadPoolAliasName;
    }

    public void setThreadPoolAliasName(String threadPoolAliasName) {
        this.threadPoolAliasName = threadPoolAliasName;
    }


    public void setRejectHandlerName(String rejectHandlerName) {
        this.rejectHandlerName = rejectHandlerName;
    }

    public void setNotifyItems(List<NotifyItem> notifyItems) {
        this.notifyItems = notifyItems;
    }

    public void setNotifyEnabled(boolean notifyEnabled) {
        this.notifyEnabled = notifyEnabled;
    }

    public void setPreStartAllCoreThreads(boolean preStartAllCoreThreads) {
        this.preStartAllCoreThreads = preStartAllCoreThreads;
    }

    public String getQueueName() {
        return getQueue().getClass().getSimpleName();
    }

    public int getQueueCapacity() {
        int capacity = getQueue().size() + getQueue().remainingCapacity();
        return capacity < 0 ? Integer.MAX_VALUE : capacity;
    }

    public String getRejectHandlerName() {
        return rejectHandlerName;
    }

    /**
     * TODO
     * Java有很多并发控制机制，比如说以AQS为基础的锁或者以CAS为原理的自旋锁。不了解AQS的朋友可以阅读我之前的AQS源码解析文章。一般来说，CAS适合轻量级的并发操作，也就是并发量并不多，而且等待时间不长的情况，否则就应该使用普通锁，进入阻塞状态，避免CPU空转。
     *
     * 所以，如果你有一个Long类型的值会被多线程修改，那么使用CAS进行并发控制比较好，但是如果你是需要锁住一些资源，然后进行数据库操作，那么还是使用阻塞锁比较好。
     *
     * 第一种情况下，我们一般都使用AtomicLong。AtomicLong是通过无限循环不停的采取CAS的方法去设置内部的value，直到成功为止。那么当并发数比较多或出现更新热点时，就会导致CAS的失败机率变高，重试次数更多，越多的线程重试，CAS失败的机率越高，形成恶性循环，从而降低了效率。
     *
     * 而LongAdder的原理就是降低对value更新的并发数，也就是将对单一value的变更压力分散到多个value值上，降低单个value的“热度”。
     *
     * 我们知道LongAdder的大致原理之后，再来详细的了解一下它的具体实现，其中也有很多值得借鉴的并发编程的技巧。
     */
    private final LongAdder runTimeoutCount = new LongAdder();

    private final LongAdder queueTimeoutCount = new LongAdder();

    public DtpExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public DtpExecutor(int corePoolSize,
                       int maximumPoolSize,
                       long keepAliveTime,
                       TimeUnit unit,
                       BlockingQueue<Runnable> workQueue,
                       ThreadFactory threadFactory,
                       RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.rejectHandlerName = handler.getClass().getSimpleName();
        setRejectedExecutionHandler(RejectHandlerGetter.getProxy(handler));
    }

    @Override
    public void execute(Runnable command) {
        String taskName = null;
        String groupName = null;
        if(command instanceof DtpRunnable){
            taskName = ((DtpRunnable)command).getName();
            groupName = ((DtpRunnable)command).getName();
        }
        //todo taskWrappers
        if(runTimeout>0 || queueTimeout>0){
            command = new DtpRunnable(command,groupName,taskName);
        }
        super.execute(command);
    }

    @Override
    protected void initialize(DtpProperties properties) {

    }


    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        if(!(r instanceof DtpRunnable)){
            super.beforeExecute(t,r);
            return;
        }
        DtpRunnable runnable = (DtpRunnable) r;
        long curTime = System.currentTimeMillis();
        if(runTimeout>0){
            runnable.setStartTime(curTime);
        }
        if(queueTimeout>0){
            long waitTime = curTime-runnable.getSubmitTime();
            if(waitTime>queueTimeout){
                queueTimeoutCount.increment();
                //TODO 按照一种策略，目前是超过就报警，重新计数
                if(queueTimeoutCount.intValue()>queueTimeoutThreshold){
                    //TODO 触发警报
                    queueTimeoutCount.reset();
                }
                log.warn("task "+runnable.getName()+" wait timeout"+""+" in executor "+this.getThreadPoolName()+
                        ",timeout "+waitTime+"ms");
            }
        }
        super.beforeExecute(t,r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if(queueTimeout>0){
            DtpRunnable runnable = (DtpRunnable) r;
            long runTime = System.currentTimeMillis()-runnable.getStartTime();
            if(runTime>runTimeout){
                runTimeoutCount.increment();
                if(runTimeoutCount.intValue()>runTimeoutThreshold){
                    //TODO 触发警报
                    runTimeoutCount.reset();
                }
                log.warn("task "+runnable.getName()+" execute timeout"+""+" in executor "+this.getThreadPoolName()+
                        ",timeout "+runTime+"ms");
            }
        }
        super.afterExecute(r, t);
    }

    public void incRejectCount(int count) {
        rejectCount.add(count);
    }

    public long getRejectCount() {
        return rejectCount.sum();
    }

}
