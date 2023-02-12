package com.bsren.dtp.queue;

import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class VariableLinkedBlockingQueue<E> extends AbstractQueue<E> implements
        BlockingQueue<E>, Serializable {

    private static final long serialVersionUID = -6903933977591709194L;

    static class Node<E>{
        volatile E item;

        Node<E> next;

        Node(E e){
            item = e;
        }
    }

    private int capacity;

    private final AtomicInteger counter = new AtomicInteger(0);

    private transient Node<E> head;

    private transient Node<E> tail;

    private final ReentrantLock takeLock = new ReentrantLock();

    private final Condition notEmpty = takeLock.newCondition();

    private final ReentrantLock putLock = new ReentrantLock();

    private final Condition notFull = putLock.newCondition();

    private void signalNotFull(){
        final ReentrantLock lock = this.putLock;
        lock.lock();
        try {
            notFull.signal();
        }finally {
            lock.unlock();
        }
    }

    private void signalNotEmpty(){
        final ReentrantLock lock = this.takeLock;
        lock.lock();
        try {
            notEmpty.signal();
        }finally {
            lock.unlock();
        }
    }

    /**
     * must hold lock
     */
    private void insert(E e){
        tail.next = new Node<>(e);
        tail = tail.next;
    }

    /**
     * must hold lock
     */
    private E extract(){
        Node<E> first = head.next;
        head = first;
        E e = first.item;
        first.item = null;
        return e;
    }

    public void fullyLock(){
        takeLock.lock();
        putLock.lock();
    }

    public void fullyUnlock(){
        takeLock.unlock();
        putLock.unlock();
    }

    public VariableLinkedBlockingQueue(){
        this(Integer.MAX_VALUE);
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    public VariableLinkedBlockingQueue(int capacity){
        if(capacity<=0){
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        head = tail = new Node<>(null);
    }


    public VariableLinkedBlockingQueue(Collection<? extends E> c){
        this(Integer.MAX_VALUE);
        this.addAll(c);
    }

    @Override
    public int size() {
        return counter.get();
    }

    /**
     * 重新设置capacity
     */
    public void setCapacity(int capacity) {
        final int oldCapacity = this.capacity;
        this.capacity = capacity;
        int size = counter.get();
        if(capacity>oldCapacity){
            signalNotFull();
        }
    }

    @Override
    public int remainingCapacity() {
        return this.capacity-counter.get();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }

    @Override
    public void put(E e) throws InterruptedException {
        if(e==null){
            throw new NullPointerException();
        }
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.counter;
        putLock.lockInterruptibly();
        try {
            while (count.get()==capacity){
                notEmpty.await();
            }
            c = count.getAndIncrement();
            insert(e);
            if(c+1<capacity){
                notFull.signal();
            }
        }finally {
            putLock.unlock();
        }
        if(c==0){
            signalNotEmpty();
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if(e==null){
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.counter;
        int c = -1;
        putLock.lockInterruptibly();
        try {
            while (count.get()==this.capacity){
                if(nanos<0){
                    return false;
                }
                nanos = unit.toNanos(nanos);
            }
            insert(e);
            c = count.getAndIncrement();
            if(c+1<capacity){
                notFull.signal();
            }
        }finally {
            putLock.unlock();
        }
        if(c==0){
            signalNotEmpty();
        }
        return true;
    }


    @Override
    public boolean offer(E e) {
        if(e==null){
            throw new NullPointerException();
        }
        final AtomicInteger count = this.counter;
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        int c = -1;
        try {
            if(count.get()<capacity){
                insert(e);
                c = count.getAndIncrement();
                if(c+1<capacity){
                    notFull.signal();
                }
            }
        }finally {
            putLock.unlock();
        }
        if(c==0){
            signalNotEmpty();
        }
        return c>=0;
    }

    @Override
    public E take() throws InterruptedException {
        E x = null;
        final ReentrantLock takeLock = this.takeLock;
        final AtomicInteger count = this.counter;
        int c = -1;
        takeLock.lockInterruptibly();
        try {
            while (count.get()==0){
                notEmpty.await();
            }
            x = extract();
            c = count.getAndDecrement();
            if(c>1){
                notEmpty.signal();
            }
        }finally {
            takeLock.unlock();
        }
        if(c==capacity){
            signalNotFull();
        }
        return x;
    }


    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        final AtomicInteger count = this.counter;
        long nanos = unit.toNanos(timeout);
        takeLock.lockInterruptibly();
        try {
            while (count.get()==0){
                if(nanos<0){
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            x = extract();
            c = count.getAndDecrement();
            if(c>1){
                notEmpty.signal();
            }
        }finally {
            takeLock.unlock();
        }
        if(c==capacity){
            signalNotFull();
        }
        return x;
    }

    @Override
    public E poll() {
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        final AtomicInteger count = this.counter;
        takeLock.lock();
        try {
            if(count.get()==0){
                return null;
            }
            x = extract();
            c = count.getAndDecrement();
            if(c>1){
                notEmpty.signal();
            }
        }finally {
            takeLock.unlock();
        }
        if(c==capacity){
            signalNotFull();
        }
        return x;
    }

    @Override
    public E peek() {
        final AtomicInteger count = this.counter;
        if(count.get()==0){
            return null;
        }
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            return count.get()>0?head.next.item:null;
        }finally {
            takeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        if(o==null){
            return false;
        }
        boolean removed = false;
        fullyLock();
        try {
            Node<E> trail = head;
            Node<E> p = head.next;
            while (p!=null){
                if(p.item.equals(o)){
                    removed = true;
                    break;
                }
                trail = p;
                p = p.next;
            }
            if(removed){
                p.item = null;
                trail.next = p.next;
                if(tail==p){
                    tail = trail;
                }
                if(counter.getAndDecrement()==capacity){
                    notFull.signal();
                }
            }
        }finally {
            fullyUnlock();
        }
        return removed;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            int size = counter.get();
            if(a.length<size){
                a = (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
            }
            int k = 0;
            for (Node<E> p = head.next;p!=null;p = p.next){
                a[k++] = (T) p.item;
            }
            if (a.length > k)
                a[k] = null;
            return a;
        }finally {
            fullyUnlock();
        }
    }

    @Override
    public Object[] toArray() {
        fullyLock();
        try {
            int size = counter.get();
            Object[] a = new Object[size];
            int k = 0;
            for (Node<E> p = head.next;p!=null;p=p.next){
                a[k++] = p.item;
            }
            return a;
        }finally {
            fullyUnlock();
        }
    }

    @Override
    public void clear() {
        fullyLock();
        try {
            for (Node<E> p, h = head; (p = h.next) != null; h = p) {
                h.next = h;
                p.item = null;
            }
            head = tail;
            // assert head.item == null && head.next == null;
            if (counter.getAndSet(0) == capacity)
                notFull.signal();
        } finally {
            fullyUnlock();
        }
    }
}
