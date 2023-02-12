package com.bsren.dtp.queue;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class MemorySafeLinkedBlockingQueue<E> extends VariableLinkedBlockingQueue<E> {

    private static final long serialVersionUID = 8032578371739960142L;

    public static final int THE_256_MB = 256 * 1024 * 1024;

    private int maxFreeMemory;

    public MemorySafeLinkedBlockingQueue() {
        this(THE_256_MB);
    }

    public MemorySafeLinkedBlockingQueue(final int maxFreeMemory) {
        super(Integer.MAX_VALUE);
        this.maxFreeMemory = maxFreeMemory;
    }

    public MemorySafeLinkedBlockingQueue(final int capacity, final int maxFreeMemory) {
        super(capacity);
        this.maxFreeMemory = maxFreeMemory;
    }

    public void setMaxFreeMemory(final int maxFreeMemory) {
        this.maxFreeMemory = maxFreeMemory;
    }

    /**
     * get the max free memory.
     *
     * @return the max free memory limit
     */
    public int getMaxFreeMemory() {
        return maxFreeMemory;
    }

    public boolean hasRemainedMemory() {
        if (MemoryLimitCalculator.maxAvailable() > maxFreeMemory) {
            return true;
        }
        throw new RejectedExecutionException("No more memory can be used.");
    }

    @Override
    public void put(final E e) throws InterruptedException {
        if (hasRemainedMemory()) {
            super.put(e);
        }
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return hasRemainedMemory() && super.offer(e, timeout, unit);
    }

    @Override
    public boolean offer(final E e) {
        return hasRemainedMemory() && super.offer(e);
    }
}
