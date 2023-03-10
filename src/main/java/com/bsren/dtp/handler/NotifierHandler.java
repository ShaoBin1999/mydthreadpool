package com.bsren.dtp.handler;

import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.context.DtpNotifyCtxHolder;
import com.bsren.dtp.dto.DtpMainProp;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.dto.NotifyPlatform;
import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.notify.base.DingNotifier;
import com.bsren.dtp.notify.base.LarkNotifier;
import com.bsren.dtp.notify.base.WechatNotifier;
import com.bsren.dtp.notify.dtpnotifier.DtpDingNotifier;
import com.bsren.dtp.notify.dtpnotifier.DtpLarkNotifier;
import com.bsren.dtp.notify.dtpnotifier.DtpNotifier;
import com.bsren.dtp.notify.dtpnotifier.DtpWechatNotifier;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

@Slf4j
public class NotifierHandler {

    private static final Map<String, DtpNotifier> NOTIFIERS = new HashMap<>();

    private NotifierHandler(){
        ServiceLoader<DtpNotifier> loader = ServiceLoader.load(DtpNotifier.class);
        for (DtpNotifier dtpNotifier : loader) {
            NOTIFIERS.put(dtpNotifier.platform(),dtpNotifier);
        }

        DtpNotifier dingNotifier = new DtpDingNotifier(new DingNotifier());
        DtpNotifier wechatNotifier = new DtpWechatNotifier(new WechatNotifier());
        DtpNotifier larkNotifier = new DtpLarkNotifier(new LarkNotifier());
        NOTIFIERS.put(dingNotifier.platform(), dingNotifier);
        NOTIFIERS.put(wechatNotifier.platform(), wechatNotifier);
        NOTIFIERS.put(larkNotifier.platform(), larkNotifier);
    }

    public void sendNotice(DtpMainProp prop, List<String> diffs) {

        try {
            NotifyItem notifyItem = DtpNotifyCtxHolder.get().getNotifyItem();
            for (String platform : notifyItem.getPlatforms()) {
                DtpNotifier notifier = NOTIFIERS.get(platform.toLowerCase());
                if (notifier != null) {
                    notifier.sendChangeMsg(prop, diffs);
                }
            }
        } finally {
            DtpNotifyCtxHolder.remove();
        }
    }

    public void sendAlarm(NotifyItemEnum notifyItemEnum) {

        try {
            NotifyItem notifyItem = DtpNotifyCtxHolder.get().getNotifyItem();
            for (String platform : notifyItem.getPlatforms()) {
                DtpNotifier notifier = NOTIFIERS.get(platform.toLowerCase());
                if (notifier != null) {
                    notifier.sendAlarmMsg(notifyItemEnum);
                }
            }
        } finally {
            DtpNotifyCtxHolder.remove();
        }
    }

    public static NotifierHandler getInstance() {
        return NotifierHandlerHolder.INSTANCE;
    }

    private static class NotifierHandlerHolder {
        private static final NotifierHandler INSTANCE = new NotifierHandler();
    }
}
