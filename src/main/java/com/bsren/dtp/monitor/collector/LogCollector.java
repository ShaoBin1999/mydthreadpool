package com.bsren.dtp.monitor.collector;

import cn.hutool.json.JSONUtil;
import com.bsren.dtp.dto.ThreadPoolStats;
import com.bsren.dtp.em.CollectorTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * LogCollector related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Slf4j
public class LogCollector extends AbstractCollector {

    @Override
    public void collect(ThreadPoolStats threadPoolStats) {
        String metrics = JSONUtil.toJsonStr(threadPoolStats);
//        if (LogHelper.getMonitorLogger() == null) {
//            log.error("Cannot find monitor logger...");
//            return;
//        }
//        LogHelper.getMonitorLogger().info("{}", metrics);
    }

    @Override
    public String type() {
        return CollectorTypeEnum.LOGGING.name().toLowerCase();
    }
}
