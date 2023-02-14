package com.bsren.dtp.properties;

import com.bsren.dtp.dto.NotifyItem;
import lombok.Data;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SimpleTpProperties main properties.
 *
 * @author yanhom
 * @since 1.0.6
 **/
@Data
public class SimpleTpProperties {

    /**
     * Name of ThreadPool.
     */
    private String threadPoolName;

    /**
     * Simple Alias Name of  ThreadPool. Use for notify.
     */
    private String threadPoolAliasName;

    /**
     * CoreSize of ThreadPool.
     */
    private int corePoolSize;

    /**
     * MaxSize of ThreadPool.
     */
    private int maximumPoolSize;

    /**
     * When the number of threads is greater than the core,
     * this is the maximum time that excess idle threads
     * will wait for new tasks before terminating.
     */
    private long keepAliveTime = 60;

    /**
     * Timeout unit.
     */
    private TimeUnit unit = TimeUnit.SECONDS;

    private List<NotifyItem> notifyItems;

    /**
     * If enable notify.
     */
    private boolean notifyEnabled = true;
}
