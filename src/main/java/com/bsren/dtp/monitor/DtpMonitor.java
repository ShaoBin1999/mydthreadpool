package com.bsren.dtp.monitor;

import com.bsren.dtp.DtpRegistry;
import com.bsren.dtp.convert.MetricsConverter;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.ThreadPoolStats;
import com.bsren.dtp.event.AlarmCheckEvent;
import com.bsren.dtp.event.CollectEvent;
import com.bsren.dtp.handler.CollectorHandler;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.notify.manager.AlarmManager;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.thread.DtpExecutor;
import com.bsren.dtp.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.bsren.dtp.constant.DynamicTpConst.SCHEDULE_NOTIFY_ITEMS;
import static com.bsren.dtp.notify.manager.AlarmManager.doAlarm;

@Slf4j
public class DtpMonitor implements ApplicationRunner, Ordered {

    private static final ScheduledExecutorService MONITOR_EXECUTOR = new ScheduledThreadPoolExecutor(
            1, new NamedThreadFactory("dtp-monitor", true));

    @Resource
    private DtpProperties dtpProperties;

    @Override
    public void run(ApplicationArguments args) {
        MONITOR_EXECUTOR.scheduleWithFixedDelay(this::run,
                0, dtpProperties.getMonitorInterval(), TimeUnit.SECONDS);
    }

    /**
     * 1. 拿到dtp 和 common threadPool
     * 2/
     */
    private void run() {
        List<String> dtpNames = DtpRegistry.listAllDtpNames();
        List<String> commonNames = DtpRegistry.listAllCommonNames();
        checkAlarm(dtpNames);
        collect(dtpNames, commonNames);
    }

    private void collect(List<String> dtpNames, List<String> commonNames) {
        if (!dtpProperties.isEnabledCollect()) {
            return;
        }

        dtpNames.forEach(x -> {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(x);
            ThreadPoolStats poolStats = MetricsConverter.convert(executor);
            doCollect(poolStats);
        });
        commonNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getCommonExecutor(x);
            ThreadPoolStats poolStats = MetricsConverter.convert(wrapper);
            doCollect(poolStats);
        });
        publishCollectEvent();
    }

    private void checkAlarm(List<String> dtpNames) {
        dtpNames.forEach(x -> {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(x);
            AlarmManager.triggerAlarm(() -> doAlarm(executor, SCHEDULE_NOTIFY_ITEMS));
        });
        publishAlarmCheckEvent();
    }

    private void doCollect(ThreadPoolStats threadPoolStats) {
        try {
            CollectorHandler.getInstance().collect(threadPoolStats, dtpProperties.getCollectorTypes());
        } catch (Exception e) {
            log.error("DynamicTp monitor, metrics collect error.", e);
        }
    }

    private void publishCollectEvent() {
        CollectEvent event = new CollectEvent(this, dtpProperties);
        ApplicationContextHolder.publishEvent(event);
    }

    private void publishAlarmCheckEvent() {
        AlarmCheckEvent event = new AlarmCheckEvent(this, dtpProperties);
        ApplicationContextHolder.publishEvent(event);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
