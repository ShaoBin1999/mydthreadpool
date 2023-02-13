package com.bsren.dtp.notify.alarm;

import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.em.NotifyItemEnum;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 用作计数警告的次数，避免在短时间内发生大量的警告事件
 * 键是threadPoolName+notifyType,key是notifyType
 * 默认为120s发生一次
 * TODO 进行配置
 */
public class AlarmLimiter {

    private static final Map<String, Cache<String, String>> ALARM_LIMITER = new ConcurrentHashMap<>();

    private AlarmLimiter() { }

    public static void initAlarmLimiter(String threadPoolName, NotifyItem notifyItem) {
        if (NotifyItemEnum.CONFIG_CHANGE.getValue().equalsIgnoreCase(notifyItem.getType())) {
            return;
        }

        String key = genKey(threadPoolName, notifyItem.getType());
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(notifyItem.getInterval(), TimeUnit.SECONDS)
                .build();
        ALARM_LIMITER.put(key, cache);
    }

    public static void putVal(String threadPoolName, String type) {
        String key = genKey(threadPoolName, type);
        ALARM_LIMITER.get(key).put(type, type);
    }

    public static String getAlarmLimitInfo(String key, String type) {
        return ALARM_LIMITER.get(key).getIfPresent(type);
    }

    public static boolean ifAlarm(String threadPoolName, String type) {
        String key = genKey(threadPoolName, type);
        return StringUtils.isBlank(getAlarmLimitInfo(key, type));
    }

    public static String genKey(String threadPoolName, String type) {
        return threadPoolName + ":" + type;
    }
}
