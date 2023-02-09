package com.bsren.dtp.reject;

import com.bsren.dtp.em.RejectedTypeEnum;
import com.bsren.dtp.exception.DtpException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.ServiceLoader;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import static com.bsren.dtp.em.RejectedTypeEnum.*;

@Slf4j
public class RejectHandlerGetter {

    public static RejectedExecutionHandler buildRejectedHandler(String name){
        if(ABORT_POLICY.getName().equals(name)){
            return new ThreadPoolExecutor.AbortPolicy();
        }else if(CALLER_RUNS_POLICY.getName().equals(name)){
            return new ThreadPoolExecutor.CallerRunsPolicy();
        }else if(DISCARD_OLDEST_POLICY.getName().equals(name)){
            return new ThreadPoolExecutor.DiscardOldestPolicy();
        }else if(DISCARD_POLICY.getName().equals(name)){
            return new ThreadPoolExecutor.DiscardPolicy();
        }

        ServiceLoader<RejectedExecutionHandler> handlers = ServiceLoader.load(RejectedExecutionHandler.class);
        for (RejectedExecutionHandler handler : handlers) {
            if(handler.getClass().getSimpleName().equalsIgnoreCase(name)){
                return handler;
            }
        }
        log.error("no such rejectedExecutionHandler named {}",name);
        throw new DtpException("no such rejectedExecutionHandler named "+name);
    }

    public static RejectedExecutionHandler getProxy(String name){
        return getProxy(buildRejectedHandler(name));
    }

    public static RejectedExecutionHandler getProxy(RejectedExecutionHandler handler){
        return (RejectedExecutionHandler) Proxy.newProxyInstance(
               handler.getClass().getClassLoader() ,
                new Class[]{RejectedExecutionHandler.class},
                new RejectedInvocationHandler(handler)
        );
    }
}
