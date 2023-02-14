package com.bsren.dtp.notify.alarm;

import com.bsren.dtp.dto.AlarmInfo;
import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.thread.DtpExecutor;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import static com.bsren.dtp.constant.DynamicTpConst.*;
import static com.bsren.dtp.em.NotifyItemEnum.REJECT;

/**
 * 用threadPoolName和通知项名称作为key,指向一个alarmInfo
 */
public class AlarmCounter {

    private static final String DEFAULT_COUNT_STR = UNKNOWN + " / " + UNKNOWN;

    private AlarmCounter() { }

    private static final Map<String, AlarmInfo> ALARM_INFO_MAP
             = new ConcurrentHashMap<>();

    public static void init(String threadPoolName,String notifyItemType){
        String key = buildKey(threadPoolName,notifyItemType);
        AlarmInfo alarmInfo = new AlarmInfo().setNotifyItemEnum(NotifyItemEnum.of(notifyItemType));
        ALARM_INFO_MAP.put(key,alarmInfo);
    }

    public static AlarmInfo getAlarmInfo(String threadPoolName,String notifyItemType){
        String key = buildKey(threadPoolName,notifyItemType);
        return ALARM_INFO_MAP.get(key);
    }

    public static String getCount(String threadPoolName, String notifyItemType){
        AlarmInfo alarmInfo = getAlarmInfo(threadPoolName, notifyItemType);
        if(alarmInfo!=null){
            return String.valueOf(alarmInfo.getCount());
        }
        return UNKNOWN;
    }

    public static void reset(String threadPoolName,String notifyItemType){
        AlarmInfo alarmInfo = getAlarmInfo(threadPoolName, notifyItemType);
        if(alarmInfo!=null) alarmInfo.reset();
    }

    public static void incAlarmCounter(String threadPoolName,String notifyItemType){
        AlarmInfo alarmInfo = getAlarmInfo(threadPoolName, notifyItemType);
        if(alarmInfo!=null) alarmInfo.incCounter();
    }

    private static String buildKey(String threadPoolName, String notifyItemType) {
        return threadPoolName + ":" + notifyItemType;
    }

    public static Triple<String, String, String> countStrRrq(String threadPoolName, ThreadPoolExecutor executor) {

        if (!(executor instanceof DtpExecutor)) {
            return new ImmutableTriple<>(DEFAULT_COUNT_STR, DEFAULT_COUNT_STR, DEFAULT_COUNT_STR);
        }

        DtpExecutor dtpExecutor = (DtpExecutor) executor;
        String rejectCount = getCount(threadPoolName, REJECT.getValue()) + " / " + dtpExecutor.getRejectCount();
        String runTimeoutCount = getCount(threadPoolName, NotifyItemEnum.RUN_TIMEOUT.getValue()) + " / "
                + dtpExecutor.getRunTimeoutCount();
        String queueTimeoutCount = getCount(threadPoolName, NotifyItemEnum.QUEUE_TIMEOUT.getValue()) + " / "
                + dtpExecutor.getQueueTimeoutCount();
        return new ImmutableTriple<>(rejectCount, runTimeoutCount, queueTimeoutCount);
    }
}
