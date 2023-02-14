package com.bsren.dtp.notify.manager;

import cn.hutool.core.util.NumberUtil;
import com.bsren.dtp.context.AlarmNotifyCtx;
import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.dto.AlarmInfo;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.dto.NotifyPlatform;
import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.em.RejectedTypeEnum;
import com.bsren.dtp.filter.InvokerChain;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.notify.alarm.AlarmCounter;
import com.bsren.dtp.notify.alarm.AlarmLimiter;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.support.ThreadPoolBuilder;
import com.bsren.dtp.thread.DtpExecutor;
import com.bsren.dtp.util.StreamUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

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

    private static final InvokerChain<BaseNotifyCtx> ALARM_INVOKER_CHAIN;

    static {
        ALARM_INVOKER_CHAIN = NotifyFilterBuilder.getAlarmInvokerChain();
    }

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

    public static void doAlarm(DtpExecutor executor, List<NotifyItemEnum> notifyItemEnums) {
        ExecutorWrapper executorWrapper = new ExecutorWrapper(executor.getThreadPoolName(), executor,
                executor.getNotifyItems(), executor.isNotifyEnabled());
        doAlarm(executorWrapper, notifyItemEnums);
    }

    public static void doAlarm(ExecutorWrapper executorWrapper, List<NotifyItemEnum> notifyItemEnums) {
        notifyItemEnums.forEach(x -> doAlarm(executorWrapper, x));
    }

    public static void doAlarm(DtpExecutor executor, NotifyItemEnum notifyItemEnum) {
        ExecutorWrapper executorWrapper = new ExecutorWrapper(executor.getThreadPoolName(), executor,
                executor.getNotifyItems(), executor.isNotifyEnabled());
        doAlarm(executorWrapper, notifyItemEnum);
    }

    public static void doAlarm(ExecutorWrapper executorWrapper, NotifyItemEnum notifyItemEnum) {
        NotifyItem notifyItem = NotifyItemManager.getNotifyItem(executorWrapper, notifyItemEnum);
        if (notifyItem == null) {
            return;
        }
        AlarmNotifyCtx alarmCtx = new AlarmNotifyCtx(executorWrapper, notifyItem);
        DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
        alarmCtx.setPlatforms(dtpProperties.getPlatforms());
        ALARM_INVOKER_CHAIN.proceed(alarmCtx);
    }


    public static boolean checkThreshold(ExecutorWrapper executor, NotifyItemEnum itemEnum, NotifyItem notifyItem) {

        switch (itemEnum) {
            case CAPACITY:
                return checkCapacity(executor, notifyItem);
            case LIVE:
                return checkLive(executor, notifyItem);
            case REJECT:
            case RUN_TIMEOUT:
            case QUEUE_TIMEOUT:
                return checkWithAlarmInfo(executor, notifyItem);
            default:
                log.error("Unsupported alarm type, type: {}", itemEnum);
                return false;
        }
    }

    private static boolean checkLive(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {
        val executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        int maximumPoolSize = executor.getMaximumPoolSize();
        double div = NumberUtil.div(executor.getActiveCount(), maximumPoolSize, 2) * 100;
        return div >= notifyItem.getThreshold();
    }

    public static void triggerAlarm(String dtpName, String notifyType, Runnable runnable) {
        AlarmCounter.incAlarmCounter(dtpName, notifyType);
        ALARM_EXECUTOR.execute(runnable);
    }

    public static void triggerAlarm(Runnable runnable) {
        ALARM_EXECUTOR.execute(runnable);
    }

    private static boolean checkCapacity(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {

        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        BlockingQueue<Runnable> workQueue = executor.getQueue();
        if (CollectionUtils.isEmpty(workQueue)) {
            return false;
        }

        int queueCapacity = executor.getQueue().size() + executor.getQueue().remainingCapacity();
        double div = NumberUtil.div(workQueue.size(), queueCapacity, 2) * 100;
        return div >= notifyItem.getThreshold();
    }

    private static boolean checkWithAlarmInfo(ExecutorWrapper executorWrapper, NotifyItem notifyItem) {
        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(), notifyItem.getType());
        return alarmInfo.getCount() >= notifyItem.getThreshold();
    }

    public static void refreshAlarm(String poolName,
                                    List<NotifyPlatform> platforms,
                                    List<NotifyItem> oldItems,
                                    List<NotifyItem> newItems) {
        if (CollectionUtils.isEmpty(newItems)) {
            return;
        }
        NotifyItemManager.fillPlatforms(platforms, newItems);
        Map<String, NotifyItem> oldNotifyItemMap = StreamUtil.toMap(oldItems, NotifyItem::getType);
        newItems.forEach(x -> {
            NotifyItem oldNotifyItem = oldNotifyItemMap.get(x.getType());
            if (Objects.nonNull(oldNotifyItem) && oldNotifyItem.getInterval() == x.getInterval()) {
                return;
            }
            AlarmLimiter.initAlarmLimiter(poolName, x);
            AlarmCounter.init(poolName, x.getType());
        });
    }
}
