package com.bsren.dtp.context;

import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.dto.AlarmInfo;

/**
 * 继承baseNotifyCtx, 包装了alarmInfo
 */
public class AlarmNotifyCtx extends BaseNotifyCtx{

    private AlarmInfo alarmInfo;

    public AlarmInfo getAlarmInfo() {
        return alarmInfo;
    }

    public void setAlarmInfo(AlarmInfo alarmInfo) {
        this.alarmInfo = alarmInfo;
    }

    public AlarmNotifyCtx(ExecutorWrapper wrapper, NotifyItem notifyItem) {
        super(wrapper, notifyItem);
    }
}
