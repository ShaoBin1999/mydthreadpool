package com.bsren.dtp.reject;

import com.bsren.dtp.thread.DtpExecutor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectedInvocationHandler implements InvocationHandler {

    private final RejectedExecutionHandler handler;

    public RejectedInvocationHandler(RejectedExecutionHandler handler) {
        this.handler = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ThreadPoolExecutor executors = (ThreadPoolExecutor) args[1];
        beforeReject(executors);
        return method.invoke(handler,args);
    }

    //todo
    //触发警报
    private void beforeReject(ThreadPoolExecutor executors) {
        if(executors instanceof DtpExecutor){

        }
    }
}
