package com.bsren.dtp.filter;

public interface Filter<T> {

    int getOrder();

    void doFilter(T context, Invoker<T> nextInvoker);

}
