package com.bsren.dtp.notify.base;

import com.bsren.dtp.dto.NotifyPlatform;

public interface Notifier {

    String platform();

    void send(NotifyPlatform platform,String msg);
}
