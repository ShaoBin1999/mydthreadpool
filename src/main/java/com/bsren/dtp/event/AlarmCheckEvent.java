package com.bsren.dtp.event;
import com.bsren.dtp.properties.DtpProperties;
import org.springframework.context.ApplicationEvent;

/**
 * AlarmCheckEvent related
 *
 * @author yanhom
 * @since 1.0.0
 */
public class AlarmCheckEvent extends ApplicationEvent {

    private final DtpProperties dtpProperties;

    public AlarmCheckEvent(Object source, DtpProperties dtpProperties) {
        super(source);
        this.dtpProperties = dtpProperties;
    }

    public DtpProperties getDtpProperties() {
        return dtpProperties;
    }
}
