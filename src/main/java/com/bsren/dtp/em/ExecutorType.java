package com.bsren.dtp.em;

import com.bsren.dtp.thread.DtpExecutor;
import com.bsren.dtp.thread.EagerDtpExecutor;
import com.bsren.dtp.thread.OrderedDtpExecutor;
import lombok.Getter;

/**
 * ExecutorType related
 *
 * @author yanhom
 * @since 1.0.4
 **/
@Getter
public enum ExecutorType {

    /**
     * Executor type.
     */
    COMMON("common", DtpExecutor.class),
    EAGER("eager", EagerDtpExecutor.class),

    ORDERED("ordered", OrderedDtpExecutor.class);

    private final String name;

    private final Class<?> clazz;

    ExecutorType(String name, Class<?> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public static Class<?> getClass(String name) {
        for (ExecutorType type : ExecutorType.values()) {
            if (type.name.equals(name)) {
                return type.getClazz();
            }
        }
        return COMMON.getClazz();
    }
}
