package com.bsren.dtp.alarm;
import cn.hutool.core.date.DateUtil;
import com.bsren.dtp.em.NotifyItemEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@Accessors(chain = true)
public class AlarmCounter {

    private NotifyItemEnum notifyItemEnum;

    private String lastAlarmTime;

    private final AtomicInteger counter = new AtomicInteger(0);

    public void reset(){
        lastAlarmTime = DateUtil.now();
        counter.set(0);
    }

    public int getCount(){
        return counter.get();
    }

}
