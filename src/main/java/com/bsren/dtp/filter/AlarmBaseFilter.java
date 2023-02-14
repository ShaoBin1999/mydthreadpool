package com.bsren.dtp.filter;

import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.notify.alarm.AlarmLimiter;
import com.bsren.dtp.notify.manager.AlarmManager;
import com.bsren.dtp.notify.manager.NotifyItemManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Objects;

@Slf4j
public class AlarmBaseFilter implements NotifyFilter{

    private static final Object SEND_LOCK = new Object();

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void doFilter(BaseNotifyCtx context, Invoker<BaseNotifyCtx> nextInvoker) {
        ExecutorWrapper executorWrapper = context.getExecutorWrapper();
        NotifyItemEnum notifyItemEnum = context.getNotifyItemEnum();
        NotifyItem notifyItem = NotifyItemManager.getNotifyItem(executorWrapper, notifyItemEnum);
        if (Objects.isNull(notifyItem) || !satisfyBaseCondition(notifyItem, executorWrapper)) {
            return;
        }

        boolean ifAlarm = AlarmLimiter.ifAlarm(executorWrapper.getThreadPoolName(), notifyItemEnum.getValue());
        if (!ifAlarm) {
            log.debug("DynamicTp notify, alarm limit, dtpName: {}, notifyItem: {}",
                    executorWrapper.getThreadPoolName(), notifyItemEnum.getValue());
            return;
        }

        if (!AlarmManager.checkThreshold(executorWrapper, notifyItemEnum, notifyItem)) {
            return;
        }
        synchronized (SEND_LOCK) {
            // recheck alarm limit.
            ifAlarm = AlarmLimiter.ifAlarm(executorWrapper.getThreadPoolName(), notifyItemEnum.getValue());
            if (!ifAlarm) {
                log.warn("DynamicTp notify, concurrent send, alarm limit, dtpName: {}, notifyItem: {}",
                        executorWrapper.getThreadPoolName(), notifyItemEnum.getValue());
                return;
            }
            AlarmLimiter.putVal(executorWrapper.getThreadPoolName(), notifyItemEnum.getValue());
        }
        nextInvoker.invoke(context);
    }

    public boolean satisfyBaseCondition(NotifyItem notifyItem, ExecutorWrapper executor) {
        return executor.isNotifyEnabled()
                && notifyItem.isEnabled()
                && CollectionUtils.isNotEmpty(notifyItem.getPlatforms());
    }
}
