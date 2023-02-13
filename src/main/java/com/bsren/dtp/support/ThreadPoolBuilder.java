package com.bsren.dtp.support;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.bsren.dtp.constant.DynamicTpConst;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.em.QueueTypeEnum;
import com.bsren.dtp.exception.DtpException;
import com.bsren.dtp.queue.TaskQueue;
import com.bsren.dtp.reject.RejectHandlerGetter;
import com.bsren.dtp.thread.DtpExecutor;
import com.bsren.dtp.thread.EagerDtpExecutor;
import com.bsren.dtp.thread.NamedThreadFactory;
import com.bsren.dtp.thread.OrderedDtpExecutor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.*;

/**
 * builder for creating a ThreadPoolExecutor
 */
public class ThreadPoolBuilder {

    private String threadPoolName = "DynamicTp";

    private int corePoolSize = 1;

    private int maximumPoolSize = DynamicTpConst.AVAILABLE_PROCESSORS;

    private long keepAliveTime = 30;

    private TimeUnit timeUnit = TimeUnit.SECONDS;

    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(1024);

    private int queueCapacity = 1024;

    private int maxFreeMemory = 256;

    private ThreadFactory threadFactory = new NamedThreadFactory("dtp");

    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    private boolean allowCoreThreadTimeOut = false;

    private boolean dynamic = true;

    private boolean waitForTasksToCompleteOnShutdown = false;

    private int awaitTerminationSeconds = 0;

    private boolean ioIntensive = false;

    private boolean ordered = false;

    private boolean preStartAllCoreThreads = false;

    private long runTimeout = 0;

    private long queueTimeout = 0;

    private List<NotifyItem> notifyItems = NotifyItem.getAllNotifyItems();

    public ThreadPoolBuilder() { }

    public static ThreadPoolBuilder newBuilder() {
        return new ThreadPoolBuilder();
    }

    public ThreadPoolBuilder threadPoolName(String poolName) {
        this.threadPoolName = poolName;
        return this;
    }

    public ThreadPoolBuilder corePoolSize(int corePoolSize) {
        if (corePoolSize >= 0) {
            this.corePoolSize = corePoolSize;
        }else {
            throw new DtpException("illegal args");
        }
        return this;
    }

    public ThreadPoolBuilder maximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize > 0) {
            this.maximumPoolSize = maximumPoolSize;
        }else {
            throw new DtpException("illegal args");
        }
        return this;
    }

    public ThreadPoolBuilder keepAliveTime(long keepAliveTime) {
        if (keepAliveTime > 0) {
            this.keepAliveTime = keepAliveTime;
        }else {
            throw new DtpException("illegal args");
        }
        return this;
    }

    public ThreadPoolBuilder timeUnit(TimeUnit timeUnit) {
        if (timeUnit != null) {
            this.timeUnit = timeUnit;
        }else {
            throw new DtpException("illegal args");
        }
        return this;
    }

    public ThreadPoolBuilder workQueue(String queueName, Integer capacity, Boolean fair, Integer maxFreeMemory) {
        if (StringUtils.isNotBlank(queueName)) {
            workQueue = QueueTypeEnum.buildBlockingQueue(
                    queueName,
                    capacity != null ? capacity : this.queueCapacity,
                    fair != null && fair,
                    maxFreeMemory != null ? maxFreeMemory : this.maxFreeMemory
            );
        }
        return this;
    }

    public ThreadPoolBuilder workQueue(String queueName, Integer capacity, Boolean fair) {
        if (StringUtils.isNotBlank(queueName)) {
            workQueue = QueueTypeEnum.buildBlockingQueue(queueName,
                    capacity != null ? capacity : this.queueCapacity,
                    fair != null && fair,
                    maxFreeMemory);
        }
        return this;
    }

    public ThreadPoolBuilder queueCapacity(int queueCapacity) {
        if(queueCapacity>0){
            this.queueCapacity = queueCapacity;
        }else {
            throw new DtpException("illegal args");
        }
        return this;
    }

    public ThreadPoolBuilder maxFreeMemory(int maxFreeMemory) {
        if(maxFreeMemory>0){
            this.maxFreeMemory = maxFreeMemory;
        }else {
            throw new DtpException("illegal args");
        }
        return this;
    }

    public ThreadPoolBuilder rejectedExecutionHandler(String rejectedName) {
        if (StringUtils.isNotBlank(rejectedName)) {
            rejectedExecutionHandler = RejectHandlerGetter.buildRejectedHandler(rejectedName);
        }
        return this;
    }

    public ThreadPoolBuilder threadFactory(String prefix) {
        if (StringUtils.isNotBlank(prefix)) {
            threadFactory = new NamedThreadFactory(prefix);
        }
        return this;
    }

    public ThreadPoolBuilder allowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    public ThreadPoolBuilder dynamic(boolean dynamic) {
        this.dynamic = dynamic;
        return this;
    }

    public ThreadPoolBuilder awaitTerminationSeconds(int awaitTerminationSeconds) {
        if(awaitTerminationSeconds>0){
            this.awaitTerminationSeconds = awaitTerminationSeconds;
        }else {
            throw new DtpException("illegal args");
        }
        return this;
    }

    public ThreadPoolBuilder waitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
        return this;
    }

    public ThreadPoolBuilder ioIntensive(boolean ioIntensive) {
        this.ioIntensive = ioIntensive;
        return this;
    }

    public ThreadPoolBuilder ordered(boolean ordered) {
        this.ordered = ordered;
        return this;
    }

    public ThreadPoolBuilder preStartAllCoreThreads(boolean preStartAllCoreThreads) {
        this.preStartAllCoreThreads = preStartAllCoreThreads;
        return this;
    }

    public ThreadPoolBuilder runTimeout(long runTimeout) {
        if(runTimeout>0){
            this.runTimeout = runTimeout;
        }else {
            throw new DtpException("illegal args");
        }
        return this;
    }

    public ThreadPoolBuilder queueTimeout(long queueTimeout) {
        if(queueTimeout>0){
            this.queueTimeout = queueTimeout;
        }else {
            throw new DtpException("illegal args");
        }
        return this;
    }

    public ThreadPoolBuilder notifyItems(List<NotifyItem> notifyItemList) {
        if (CollectionUtils.isNotEmpty(notifyItemList)) {
            notifyItems = notifyItemList;
        }
        return this;
    }

    public ThreadPoolExecutor build() {
        if (dynamic) {
            return buildDtpExecutor(this);
        } else {
            return buildCommonExecutor(this);
        }
    }

    public DtpExecutor buildDynamic() {
        return buildDtpExecutor(this);
    }

    public ThreadPoolExecutor buildCommon() {
        return buildCommonExecutor(this);
    }

    public ExecutorService buildWithTtl() {
        if (dynamic) {
            return buildDtpExecutor(this);
        } else {
            return TtlExecutors.getTtlExecutorService(buildCommonExecutor(this));
        }
    }

    private DtpExecutor buildDtpExecutor(ThreadPoolBuilder builder) {
        Assert.notNull(builder.threadPoolName, "The thread pool name must not be null.");
        DtpExecutor dtpExecutor = createInternal(builder);
        dtpExecutor.setThreadPoolName(builder.threadPoolName);
        dtpExecutor.allowCoreThreadTimeOut(builder.allowCoreThreadTimeOut);
        dtpExecutor.setWaitForTasksToCompleteOnShutdown(builder.waitForTasksToCompleteOnShutdown);
        dtpExecutor.setAwaitTerminationSeconds(builder.awaitTerminationSeconds);
        dtpExecutor.setPreStartAllCoreThreads(builder.preStartAllCoreThreads);
        dtpExecutor.setRunTimeout(builder.runTimeout);
        dtpExecutor.setQueueTimeout(builder.queueTimeout);
        dtpExecutor.setNotifyItems(builder.notifyItems);
        return dtpExecutor;
    }

    private DtpExecutor createInternal(ThreadPoolBuilder builder) {
        DtpExecutor dtpExecutor;
        if (ioIntensive) {
            TaskQueue taskQueue = new TaskQueue(builder.queueCapacity);
            dtpExecutor = new EagerDtpExecutor(
                    builder.corePoolSize,
                    builder.maximumPoolSize,
                    builder.keepAliveTime,
                    builder.timeUnit,
                    taskQueue,
                    builder.threadFactory,
                    builder.rejectedExecutionHandler);
            taskQueue.setExecutor((EagerDtpExecutor) dtpExecutor);
        } else if (ordered) {
            dtpExecutor = new OrderedDtpExecutor(
                    builder.corePoolSize,
                    builder.maximumPoolSize,
                    builder.keepAliveTime,
                    builder.timeUnit,
                    builder.workQueue,
                    builder.threadFactory,
                    builder.rejectedExecutionHandler);
        } else {
            dtpExecutor = new DtpExecutor(
                    builder.corePoolSize,
                    builder.maximumPoolSize,
                    builder.keepAliveTime,
                    builder.timeUnit,
                    builder.workQueue,
                    builder.threadFactory,
                    builder.rejectedExecutionHandler);
        }
        return dtpExecutor;
    }

    private ThreadPoolExecutor buildCommonExecutor(ThreadPoolBuilder builder) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                builder.corePoolSize,
                builder.maximumPoolSize,
                builder.keepAliveTime,
                builder.timeUnit,
                builder.workQueue,
                builder.threadFactory,
                builder.rejectedExecutionHandler
        );
        executor.allowCoreThreadTimeOut(builder.allowCoreThreadTimeOut);
        return executor;
    }
}
