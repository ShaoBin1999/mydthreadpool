package com.bsren.dtp.monitor.collector;

import cn.hutool.json.JSONUtil;
import com.bsren.dtp.dto.ThreadPoolStats;
import com.bsren.dtp.em.CollectorTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Redick01
 */
@Slf4j
public class InternalLogCollector extends AbstractCollector {

    @Override
    public void collect(ThreadPoolStats poolStats) {
        log.info("dynamic.tp metrics: {}", JSONUtil.toJsonStr(poolStats));
    }

    @Override
    public String type() {
        return CollectorTypeEnum.INTERNAL_LOGGING.name().toLowerCase();
    }
}
