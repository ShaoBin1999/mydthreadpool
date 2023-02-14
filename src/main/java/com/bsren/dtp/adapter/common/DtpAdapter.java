package com.bsren.dtp.adapter.common;

import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.properties.SimpleTpProperties;
import com.bsren.dtp.support.MetricsAware;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;

public interface DtpAdapter extends MetricsAware {

    /**
     * Get specify thread pool executor wrapper.
     *
     * @return specify executor
     */
    default ExecutorWrapper getExecutorWrapper() {
        return null;
    }

    /**
     * Get executor wrappers.
     *
     * @return executors
     */
    default Map<String, ExecutorWrapper> getExecutorWrappers() {
        return Collections.emptyMap();
    }

    /**
     * Refresh the thread pool with specify properties.
     *
     * @param dtpProperties the targeted dtpProperties
     */
    void refresh(DtpProperties dtpProperties);

    /**
     * Check update params.
     *
     * @param properties the targeted properties
     * @param log logger
     * @return true or false
     */
    default boolean containsInvalidParams(SimpleTpProperties properties, Logger log) {
        if (properties.getCorePoolSize() < 0
                || properties.getMaximumPoolSize() <= 0
                || properties.getMaximumPoolSize() < properties.getCorePoolSize()
                || properties.getKeepAliveTime() < 0) {
            log.error("DynamicTp adapter refresh, invalid parameters exist, properties: {}", properties);
            return true;
        }
        return false;
    }
}
