package com.bsren.dtp.queue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 计算可用剩余内存
 */
public class MemoryLimitCalculator {

    private static volatile long maxAvailable;

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    static {
        refresh();
        SCHEDULER.scheduleWithFixedDelay(MemoryLimitCalculator::refresh, 50, 50, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(SCHEDULER::shutdown));
    }

    private static void refresh() {
        maxAvailable = Runtime.getRuntime().freeMemory();
    }
    public static long maxAvailable() {
        return maxAvailable;
    }
}
