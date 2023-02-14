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

    public List<NotifyItem> getNotifyItems() {
        return notifyItems;
    }

    public void setNotifyItems(List<NotifyItem> notifyItems) {
        this.notifyItems = notifyItems;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isNotifyEnabled() {
        return notifyEnabled;
    }

    public void setNotifyEnabled(boolean notifyEnabled) {
        this.notifyEnabled = notifyEnabled;
    }

    private boolean notifyEnabled = true;

    public ExecutorWrapper(String threadPoolName, Executor executor) {
        this.threadPoolName = threadPoolName;
        this.executor = executor;
        this.notifyItems = NotifyItem.getSimpleNotifyItems();
    }

    public ExecutorWrapper(String threadPoolName, Executor executor, boolean notifyEnabled) {
        this.threadPoolName = threadPoolName;
        this.executor = executor;
        this.notifyItems = NotifyItem.getSimpleNotifyItems();
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
