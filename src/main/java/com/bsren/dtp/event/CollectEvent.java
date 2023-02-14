package com.bsren.dtp.event;

import com.bsren.dtp.properties.DtpProperties;
import org.springframework.context.ApplicationEvent;

/**
 * CollectEvent related
 *
 * @author yanhom
 * @since 1.0.0
 */
public class CollectEvent extends ApplicationEvent {

    private final DtpProperties dtpProperties;

    public CollectEvent(Object source, DtpProperties dtpProperties) {
        super(source);
        this.dtpProperties = dtpProperties;
    }

    public DtpProperties getDtpProperties() {
        return dtpProperties;
    }
}
