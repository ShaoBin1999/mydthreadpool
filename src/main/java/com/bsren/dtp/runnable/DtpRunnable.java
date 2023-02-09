package com.bsren.dtp.runnable;

import lombok.Data;
import lombok.Getter;

import java.util.UUID;

/**
 * 为runnable对象包装名称，应用于统计不同分组的runnable执行情况
 */
@Getter
public class DtpRunnable implements Runnable{

    private Runnable runnable;

    private String group;

    private String name;

    private final long submitTime;

    private long startTime;

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public DtpRunnable(Runnable runnable) {
        this.runnable = runnable;
        this.name = runnable.getClass().getSimpleName()+"-"+UUID.randomUUID();
        this.submitTime = System.currentTimeMillis();
    }


    public DtpRunnable(Runnable runnable, String group, String name) {
        this.runnable = runnable;
        this.group = group;
        this.name = name;
        this.submitTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        this.runnable.run();
    }

}
