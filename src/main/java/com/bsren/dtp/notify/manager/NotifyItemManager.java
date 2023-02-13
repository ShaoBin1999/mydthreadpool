package com.bsren.dtp.notify.manager;

import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.dto.NotifyPlatform;
import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.thread.DtpExecutor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.bsren.dtp.em.NotifyItemEnum.*;
import static java.util.stream.Collectors.toList;

@Slf4j
public class NotifyItemManager {

    private static final List<String> COMMON_ALARM_KEYS = Lists.newArrayList("alarmType", "threshold");

    private static final Set<String> LIVE_ALARM_KEYS = Sets.newHashSet(
            "corePoolSize", "maximumPoolSize", "poolSize", "activeCount");

    private static final Set<String> CAPACITY_ALARM_KEYS = Sets.newHashSet(
            "queueType", "queueCapacity", "queueSize", "queueRemaining");

    private static final Set<String> REJECT_ALARM_KEYS = Sets.newHashSet("rejectType", "rejectCount");

    private static final Set<String> RUN_TIMEOUT_ALARM_KEYS = Sets.newHashSet("runTimeoutCount");

    private static final Set<String> QUEUE_TIMEOUT_ALARM_KEYS = Sets.newHashSet("queueTimeoutCount");

    private static final Set<String> ALL_ALARM_KEYS;

    private static final Map<String, Set<String>> ALARM_KEYS = Maps.newHashMap();

    static {
        ALARM_KEYS.put(LIVE.name(), LIVE_ALARM_KEYS);
        ALARM_KEYS.put(CAPACITY.name(), CAPACITY_ALARM_KEYS);
        ALARM_KEYS.put(REJECT.name(), REJECT_ALARM_KEYS);
        ALARM_KEYS.put(RUN_TIMEOUT.name(), RUN_TIMEOUT_ALARM_KEYS);
        ALARM_KEYS.put(QUEUE_TIMEOUT.name(), QUEUE_TIMEOUT_ALARM_KEYS);

        ALL_ALARM_KEYS = ALARM_KEYS.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        ALL_ALARM_KEYS.addAll(COMMON_ALARM_KEYS);
    }

    private NotifyItemManager() { }

    public static Set<String> getAllAlarmKeys() {
        return ALL_ALARM_KEYS;
    }

    public static Set<String> getAlarmKeys(NotifyItemEnum notifyItemEnum) {
        Set<String> keys = ALARM_KEYS.get(notifyItemEnum.name());
        keys.addAll(COMMON_ALARM_KEYS);
        return keys;
    }

    public static NotifyItem getNotifyItem(DtpExecutor executor, NotifyItemEnum notifyItemEnum) {
         ExecutorWrapper executorWrapper = new ExecutorWrapper(executor.getThreadPoolName(), executor,
                executor.getNotifyItems(), executor.isNotifyEnabled());
        return getNotifyItem(executorWrapper, notifyItemEnum);
    }

    public static NotifyItem getNotifyItem(ExecutorWrapper executorWrapper, NotifyItemEnum notifyItemEnum) {
        List<NotifyItem> notifyItems = executorWrapper.getNotifyItems();
        Optional<NotifyItem>  notifyItemOpt = notifyItems.stream()
                .filter(x -> notifyItemEnum.getValue().equalsIgnoreCase(x.getType()))
                .findFirst();
        if (!notifyItemOpt.isPresent()) {
            log.debug("DynamicTp notify, no such [{}] notify item configured, threadPoolName: {}",
                    notifyItemEnum.getValue(), executorWrapper.getThreadPoolName());
            return null;
        }

        return notifyItemOpt.get();
    }

    /**
     * 在initAlarm的时候将platforms写到executor的notifyItems中
     */
    public static void fillPlatforms(List<NotifyPlatform> platforms, List<NotifyItem> notifyItems) {
        if (CollectionUtils.isEmpty(platforms) || CollectionUtils.isEmpty(notifyItems)) {
            log.warn("DynamicTp notify, no notify platforms or items configured.");
            return;
        }

        List<String> platformNames = platforms.stream().map(NotifyPlatform::getPlatform).collect(toList());
        notifyItems.forEach(n -> {
            if (CollectionUtils.isEmpty(n.getPlatforms())) {
                n.setPlatforms(platformNames);
            }
        });
    }
}
