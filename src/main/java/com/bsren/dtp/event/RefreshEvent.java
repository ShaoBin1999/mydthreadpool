package com.bsren.dtp.event;

import com.bsren.dtp.properties.DtpProperties;
import org.springframework.context.ApplicationEvent;

/**
 * RefreshEvent related
 *
 * @author yanhom
 * @since 1.0.0
 */
public class RefreshEvent extends ApplicationEvent {

    private final transient DtpProperties dtpProperties;

    public RefreshEvent(Object source, DtpProperties dtpProperties) {
        super(source);
        this.dtpProperties = dtpProperties;
    }

    public DtpProperties getDtpProperties() {
        return dtpProperties;
    }
}
