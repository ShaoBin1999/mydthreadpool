package com.bsren.dtp.dto;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * 线程池名称，核心线程数，最大线程数，空闲时间
 * 等待队列类型，队列长度，拒绝策略，是否允许核心线程退出
 * 用一个列表记录这8个指标的名称
 */
@Data
public class DtpMainProp {

    private static final List<Field> FIELD_NAMES;

    static {
        FIELD_NAMES = Arrays.asList(DtpMainProp.class.getDeclaredFields());
    }

    private String threadPoolName;

    private int corePoolSize;

    private int maxPoolSize;

    private long keepAliveTime;

    private String queueType;

    private int queueCapacity;

    private String rejectType;

    private boolean allowCoreThreadTimeOut;

    public static List<Field> getMainProps() {
        return FIELD_NAMES;
    }
}

