package com.bsren.dtp.notify.base;

import com.bsren.dtp.dto.NotifyPlatform;
import com.bsren.dtp.em.NotifyPlatformEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class DingNotifier implements Notifier{


    @Override
    public String platform() {
        return NotifyPlatformEnum.DING.name().toLowerCase();
    }

    @Override
    public void send(NotifyPlatform platform, String msg) {
        //todo
        log.info("send msg to ding"+"receives:"+ Arrays.toString(platform.getReceivers().split(",")));
        log.info(msg);
    }
}
