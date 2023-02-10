package com.bsren.dtp.notify.alarm;

import com.bsren.dtp.em.NotifyItemEnum;
import org.omg.CORBA.UNKNOWN;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.bsren.dtp.constant.DynamicTpConst.UNKNOWN;
/**
 * 用threadPoolName和通知项名称作为key,指向一个alarmInfo
 * 用作计数警告的次数
 */
public class AlarmCounter {


    private static final Map<String,AlarmInfo> ALARM_INFO_MAP
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
}
