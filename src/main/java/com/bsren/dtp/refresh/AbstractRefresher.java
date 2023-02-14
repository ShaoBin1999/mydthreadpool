package com.bsren.dtp.refresh;

import com.bsren.dtp.registry.DtpRegistry;
import com.bsren.dtp.em.ConfigFileTypeEnum;
import com.bsren.dtp.event.RefreshEvent;
import com.bsren.dtp.handler.ConfigHandler;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.support.PropertiesBinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
public abstract class AbstractRefresher implements Refresher {

    @Resource
    protected DtpProperties dtpProperties;

    @Override
    public void refresh(String content, ConfigFileTypeEnum fileType) {

        if (StringUtils.isBlank(content) || Objects.isNull(fileType)) {
            log.warn("DynamicTp refresh, empty content or null fileType.");
            return;
        }

        try {
            ConfigHandler configHandler = ConfigHandler.getInstance();
            Map<Object,Object> properties = configHandler.parseConfig(content, fileType);
            doRefresh(properties);
        } catch (IOException e) {
            log.error("DynamicTp refresh error, content: {}, fileType: {}", content, fileType, e);
        }
    }

    protected void doRefresh(Map<Object, Object> properties) {
        if (MapUtils.isEmpty(properties)) {
            log.warn("DynamicTp refresh, empty properties.");
            return;
        }
        PropertiesBinder.bindDtpProperties(properties, dtpProperties);
        doRefresh(dtpProperties);
    }

    protected void doRefresh(DtpProperties dtpProperties) {
        DtpRegistry.refresh(dtpProperties);
        publishEvent(dtpProperties);
    }

    private void publishEvent(DtpProperties dtpProperties) {
        RefreshEvent event = new RefreshEvent(this, dtpProperties);
        ApplicationContextHolder.publishEvent(event);
    }
}
