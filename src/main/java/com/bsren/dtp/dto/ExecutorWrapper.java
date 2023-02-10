package com.bsren.dtp.dto;

import java.util.List;
import java.util.concurrent.Executor;


/**
 * 执行器的包装类，包装了线程的名字，别名和通知项
 * notifyEnabled，是否通知全体
 */
public class ExecutorWrapper {

    private String threadPoolName;

    private Executor executor;

    private String alias;  //线程池别名

    private List<NotifyItem> notifyItems;

    private boolean notifyEnabled = true;

    public ExecutorWrapper(String threadPoolName, Executor executor) {
        this.threadPoolName = threadPoolName;
        this.executor = executor;
        this.notifyItems = NotifyItem.getBaseNotifyItems();
    }

    public ExecutorWrapper(String threadPoolName, Executor executor, boolean notifyEnabled) {
        this.threadPoolName = threadPoolName;
        this.executor = executor;
        this.notifyItems = NotifyItem.getBaseNotifyItems();
        this.notifyEnabled = notifyEnabled;
    }

    public ExecutorWrapper(String threadPoolName, Executor executor,
                           List<NotifyItem> notifyItems, boolean notifyEnabled) {
        this.threadPoolName = threadPoolName;
        this.executor = executor;
        this.notifyItems = notifyItems;
        this.notifyEnabled = notifyEnabled;
    }
}
