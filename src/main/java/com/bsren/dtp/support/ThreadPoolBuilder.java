package com.bsren.dtp.support;

import cn.hutool.core.thread.NamedThreadFactory;
import com.bsren.dtp.constant.DynamicTpConst;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.em.QueueTypeEnum;
import com.bsren.dtp.exception.DtpException;
import org.apache.commons.lang3.StringUtils;

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

//    private ThreadFactory threadFactory = new NamedThreadFactory("dtp");


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

    private ThreadPoolBuilder() { }

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
//        if (StringUtils.isNotBlank(queueName)) {
//            workQueue = QueueTypeEnum.buildLbq(queueName, capacity != null ? capacity : this.queueCapacity,
//                    fair != null && fair, maxFreeMemory != null ? maxFreeMemory : this.maxFreeMemory);
//        }
        return this;
    }
}
