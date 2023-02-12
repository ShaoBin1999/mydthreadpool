package com.bsren.dtp.queue;

import com.bsren.dtp.thread.EagerDtpExecutor;
import org.springframework.lang.NonNull;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class TaskQueue extends VariableLinkedBlockingQueue<Runnable>{
    private static final long serialVersionUID = -1L;

    private transient EagerDtpExecutor executor;

    public TaskQueue(int queueCapacity) {
        super(queueCapacity);
    }

    public void setExecutor(EagerDtpExecutor exec) {
        executor = exec;
    }

    @Override
    public boolean offer(@NonNull Runnable runnable) {
        if (executor == null) {
            throw new RejectedExecutionException("The task queue does not have executor.");
        }
        int currentPoolThreadSize = executor.getPoolSize();
        if (currentPoolThreadSize == executor.getMaximumPoolSize()) {
            return super.offer(runnable);
        }
        // have free worker. put task into queue to let the worker deal with task.
        if (executor.getSubmittedTaskCount() < currentPoolThreadSize) {
            return super.offer(runnable);
        }
        // return false to let executor create new worker.
        if (currentPoolThreadSize < executor.getMaximumPoolSize()) {
            return false;
        }
        // currentPoolThreadSize >= max
        return super.offer(runnable);
    }
    public boolean force(Runnable o, long timeout, TimeUnit unit) throws InterruptedException {
        if (executor.isShutdown()) {
            throw new RejectedExecutionException("Executor is shutdown.");
        }
        return super.offer(o, timeout, unit);
    }
}
