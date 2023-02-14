package com.bsren.dtp.filter;


public class InvokerChain<T> {

    private Invoker<T> head;

    public void proceed(T context) {
        head.invoke(context);
    }

    public void setHead(Invoker<T> head) {
        this.head = head;
    }
}


