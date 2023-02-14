package com.bsren.dtp.filter;

import com.bsren.dtp.context.AlarmNotifyCtx;
import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.context.DtpNotifyCtxHolder;
import com.bsren.dtp.dto.AlarmInfo;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.handler.NotifierHandler;
import com.bsren.dtp.notify.alarm.AlarmCounter;

public class AlarmInvoker implements Invoker<BaseNotifyCtx> {
    @Override
    public void invoke(BaseNotifyCtx context) {
        AlarmNotifyCtx ctx = (AlarmNotifyCtx) context;
        ExecutorWrapper executorWrapper = ctx.getExecutorWrapper();
        NotifyItem notifyItem = ctx.getNotifyItem();
        AlarmInfo alarmInfo = AlarmCounter.getAlarmInfo(executorWrapper.getThreadPoolName(),notifyItem.getType());
        ctx.setAlarmInfo(alarmInfo);
        DtpNotifyCtxHolder.set(ctx);
        NotifierHandler.getInstance().sendAlarm(NotifyItemEnum.of(notifyItem.getType()));
        AlarmCounter.reset(executorWrapper.getThreadPoolName(), notifyItem.getType());
    }
}
