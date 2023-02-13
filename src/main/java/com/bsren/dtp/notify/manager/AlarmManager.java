package com.bsren.dtp.notify.manager;

import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.dto.NotifyPlatform;
import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.em.RejectedTypeEnum;
import com.bsren.dtp.notify.alarm.AlarmCounter;
import com.bsren.dtp.notify.alarm.AlarmLimiter;
import com.bsren.dtp.support.ThreadPoolBuilder;
import com.bsren.dtp.thread.DtpExecutor;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.bsren.dtp.em.QueueTypeEnum.LINKED_BLOCKING_QUEUE;

@Slf4j
public class AlarmManager {

    private static final ExecutorService ALARM_EXECUTOR = ThreadPoolBuilder.newBuilder()
            .threadPoolName("dtp-alarm")
            .threadFactory("dtp-alarm")
            .corePoolSize(2)
            .maximumPoolSize(4)
            .workQueue(LINKED_BLOCKING_QUEUE.getName(), 2000, false, null)
            .rejectedExecutionHandler(RejectedTypeEnum.DISCARD_OLDEST_POLICY.getName())
            .buildCommon();

    private AlarmManager() { }

    public static void initAlarm(DtpExecutor executor, List<NotifyPlatform> platforms) {
        if (CollectionUtils.isEmpty(platforms)) {
            executor.setNotifyItems(Lists.newArrayList());
            return;
        }
        if (CollectionUtils.isEmpty(executor.getNotifyItems())) {
            log.warn("DynamicTp notify, no notify items configured, name {}", executor.getThreadPoolName());
            return;
        }
        // 将配置得到的信息放到notifyItems里
        NotifyItemManager.fillPlatforms(platforms, executor.getNotifyItems());
        // 初始化警告器和计数器
        initAlarm(executor.getThreadPoolName(), executor.getNotifyItems());
    }

    public static void initAlarm(String poolName, List<NotifyItem> notifyItems) {
        notifyItems.forEach(x -> {
            AlarmLimiter.initAlarmLimiter(poolName, x);
            AlarmCounter.init(poolName, x.getType());
        });
    }

    public static void doAlarm(DtpExecutor dtpExecutor, NotifyItemEnum reject) {

    }

    public static void triggerAlarm(String threadPoolName, String value, Runnable runnable) {

    }
}
