package com.bsren.dtp.thread;

import cn.hutool.cron.TaskExecutor;
import com.bsren.dtp.queue.TaskQueue;
import com.bsren.dtp.thread.DtpExecutor;
import org.apache.dubbo.common.threadpool.support.eager.EagerThreadPool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EagerDtpExecutor extends DtpExecutor {

    /**
     * The number of tasks submitted but not yet finished.
     */
    private final AtomicInteger submittedTaskCount = new AtomicInteger(0);

    public EagerDtpExecutor(int corePoolSize,
                            int maximumPoolSize,
                            long keepAliveTime,
                            TimeUnit unit,
                            BlockingQueue<Runnable> workQueue,
                            ThreadFactory threadFactory,
                            RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public int getSubmittedTaskCount() {
        return submittedTaskCount.get();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        submittedTaskCount.decrementAndGet();
        super.afterExecute(r, t);
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
        submittedTaskCount.incrementAndGet();
        try {
            super.execute(command);
        } catch (RejectedExecutionException rx) {
            if (getQueue() instanceof TaskQueue) {
                // If the Executor is close to maximum pool size, concurrent
                // calls to execute() may result (due to use of TaskQueue) in
                // some tasks being rejected rather than queued.
                // If this happens, add them to the queue.
                final TaskQueue queue = (TaskQueue) getQueue();
                try {
                    if (!queue.force(command, 0, TimeUnit.MILLISECONDS)) {
                        submittedTaskCount.decrementAndGet();
                        throw new RejectedExecutionException("Queue capacity is full.", rx);
                    }
                } catch (InterruptedException x) {
                    submittedTaskCount.decrementAndGet();
                    throw new RejectedExecutionException(x);
                }
            } else {
                submittedTaskCount.decrementAndGet();
                throw rx;
            }
        }
    }
}
