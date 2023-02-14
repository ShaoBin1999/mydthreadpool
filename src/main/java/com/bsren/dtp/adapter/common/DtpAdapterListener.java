package com.bsren.dtp.adapter.common;

import com.bsren.dtp.event.AlarmCheckEvent;
import com.bsren.dtp.event.CollectEvent;
import com.bsren.dtp.event.RefreshEvent;
import com.bsren.dtp.handler.CollectorHandler;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.notify.manager.AlarmManager;
import com.bsren.dtp.properties.DtpProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import static com.bsren.dtp.constant.DynamicTpConst.SCHEDULE_NOTIFY_ITEMS;
import static com.bsren.dtp.notify.manager.AlarmManager.doAlarm;

/**
 * DtpAdapterListener related
 *
 * @author yanhom
 * @since 1.0.6
 */
@Slf4j
public class DtpAdapterListener implements GenericApplicationListener {

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        Class<?> type = resolvableType.getRawClass();
        if (type != null) {
            return RefreshEvent.class.isAssignableFrom(type)
                    || CollectEvent.class.isAssignableFrom(type)
                    || AlarmCheckEvent.class.isAssignableFrom(type);
        }
        return false;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {
        try {
            if (event instanceof RefreshEvent) {
                doRefresh(((RefreshEvent) event).getDtpProperties());
            } else if (event instanceof CollectEvent) {
                doCollect(((CollectEvent) event).getDtpProperties());
            } else if (event instanceof AlarmCheckEvent) {
                doAlarmCheck(((AlarmCheckEvent) event).getDtpProperties());
            }
        } catch (Exception e) {
            log.error("DynamicTp adapter, event handle failed.", e);
        }
    }

    /**
     * Do collect thread pool stats.
     * @param dtpProperties dtpProperties
     */
    protected void doCollect(DtpProperties dtpProperties) {
        val handlerMap = ApplicationContextHolder.getBeansOfType(DtpAdapter.class);
        if (CollectionUtils.isEmpty(handlerMap)) {
            return;
        }
        handlerMap.forEach((k, v) -> v.getMultiPoolStats().forEach(ps ->
                CollectorHandler.getInstance().collect(ps, dtpProperties.getCollectorTypes())));
    }

    /**
     * Do refresh.
     * @param dtpProperties dtpProperties
     */
    protected void doRefresh(DtpProperties dtpProperties) {
        val handlerMap = ApplicationContextHolder.getBeansOfType(DtpAdapter.class);
        if (CollectionUtils.isEmpty(handlerMap)) {
            return;
        }
        handlerMap.forEach((k, v) -> v.refresh(dtpProperties));
    }

    /**
     * Do alarm check.
     * @param dtpProperties dtpProperties
     */
    protected void doAlarmCheck(DtpProperties dtpProperties) {
        val handlerMap = ApplicationContextHolder.getBeansOfType(DtpAdapter.class);
        if (CollectionUtils.isEmpty(handlerMap)) {
            return;
        }
        handlerMap.forEach((k, v) -> {
            val executorWrapper = v.getExecutorWrappers();
            executorWrapper.forEach((kk, vv) -> AlarmManager.triggerAlarm(() -> doAlarm(vv, SCHEDULE_NOTIFY_ITEMS)));
        });
    }
}
