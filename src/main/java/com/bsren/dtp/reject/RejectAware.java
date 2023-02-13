package com.bsren.dtp.reject;

import com.bsren.dtp.notify.manager.AlarmManager;
import com.bsren.dtp.thread.DtpExecutor;

import java.util.concurrent.ThreadPoolExecutor;

import static com.bsren.dtp.em.NotifyItemEnum.REJECT;

public interface RejectAware {

    default void beforeReject(ThreadPoolExecutor executor) {
        if (executor instanceof DtpExecutor) {
            DtpExecutor dtpExecutor = (DtpExecutor) executor;
            dtpExecutor.incRejectCount(1);
            Runnable runnable = () -> AlarmManager.doAlarm(dtpExecutor, REJECT);
            AlarmManager.triggerAlarm(dtpExecutor.getThreadPoolName(), REJECT.getValue(), runnable);
        }
    }


}
