package com.bsren.dtp.support;

import com.bsren.dtp.em.QueueTypeEnum;
import com.bsren.dtp.thread.DtpExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolCreator {

    private ThreadPoolCreator() { }

    public static ThreadPoolExecutor createCommonFast(String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadFactory(threadPrefix)
                .buildCommon();
    }

    public static ExecutorService createCommonWithTtl(String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .dynamic(false)
                .threadFactory(threadPrefix)
                .buildWithTtl();
    }

    public static DtpExecutor createDynamicFast(String poolName) {
        return createDynamicFast(poolName, poolName);
    }

    public static DtpExecutor createDynamicFast(String poolName, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(poolName)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    public static ExecutorService createDynamicWithTtl(String poolName) {
        return createDynamicWithTtl(poolName, poolName);
    }

    public static ExecutorService createDynamicWithTtl(String poolName, String threadPrefix) {
        return ThreadPoolBuilder.newBuilder()
                .threadPoolName(poolName)
                .threadFactory(threadPrefix)
                .buildWithTtl();
    }

    public static ThreadPoolExecutor newSingleThreadPool(String threadPrefix, int queueCapacity) {
        return newFixedThreadPool(threadPrefix, 1, queueCapacity);
    }

    public static ThreadPoolExecutor newFixedThreadPool(String threadPrefix, int poolSize, int queueCapacity) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(poolSize)
                .maximumPoolSize(poolSize)
                .workQueue(QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName(), queueCapacity, null)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    public static ExecutorService newCachedThreadPool(String threadPrefix, int maximumPoolSize) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(0)
                .maximumPoolSize(maximumPoolSize)
                .workQueue(QueueTypeEnum.SYNCHRONOUS_QUEUE.getName(), null, null)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    public static ThreadPoolExecutor newThreadPool(String threadPrefix, int corePoolSize,
                                                   int maximumPoolSize, int queueCapacity) {
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(corePoolSize)
                .maximumPoolSize(maximumPoolSize)
                .workQueue(QueueTypeEnum.VARIABLE_LINKED_BLOCKING_QUEUE.getName(), queueCapacity, null)
                .threadFactory(threadPrefix)
                .buildDynamic();
    }

    /**
     * ???????????? = ??????????????????????????????+??????CPU????????????
     * ??????????????? = CPU??????????????? / (1 - ????????????)
     * ???????????????????????????????????????0??????IO??????????????????????????????????????????1
     *
     * @param blockingCoefficient ?????????????????????????????????0~1??????????????????????????????????????????????????????????????????
     * @return {@link ThreadPoolExecutor}
     */
    public static ThreadPoolExecutor newExecutorByBlockingCoefficient(float blockingCoefficient) {
        if (blockingCoefficient >= 1 || blockingCoefficient < 0) {
            throw new IllegalArgumentException();
        }

        int poolSize = (int) (Runtime.getRuntime().availableProcessors() / (1 - blockingCoefficient));
        return ThreadPoolBuilder.newBuilder()
                .corePoolSize(poolSize)
                .maximumPoolSize(poolSize)
                .buildDynamic();
    }

}
