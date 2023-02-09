package com.bsren.dtp.thread;

import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.reject.RejectHandlerGetter;
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
    }

    @Override
    protected void initialize(DtpProperties properties) {

    }
}
