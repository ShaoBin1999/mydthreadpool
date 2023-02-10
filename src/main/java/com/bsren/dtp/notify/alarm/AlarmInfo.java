package com.bsren.dtp.notify.alarm;
import cn.hutool.core.date.DateUtil;
import com.bsren.dtp.em.NotifyItemEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * alarm的通知项，记录了通知项类型，上一次通知时间，以及总共统计的次数
 */
@Data
@Accessors(chain = true)
public class AlarmInfo {

    private NotifyItemEnum notifyItemEnum;

    private String lastAlarmTime;

    private final AtomicInteger counter = new AtomicInteger(0);

    public void incCounter(){
        counter.incrementAndGet();
    }

    public void reset(){
        lastAlarmTime = DateUtil.now();
        counter.set(0);
    }

    public int getCount(){
        return counter.get();
    }

}
