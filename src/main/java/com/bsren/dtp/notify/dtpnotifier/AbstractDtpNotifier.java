package com.bsren.dtp.notify.dtpnotifier;

import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.dto.DtpMainProp;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.notify.base.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public abstract class AbstractDtpNotifier implements DtpNotifier{

    private Notifier notifier;

    public AbstractDtpNotifier() { }

    public AbstractDtpNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void sendChangeMsg(BaseNotifyCtx ctx,DtpMainProp oldProp, DtpMainProp newProp) {
    }

    private String buildNoticeMsg(BaseNotifyCtx ctx, DtpMainProp oldProp, DtpMainProp newProp) {
        return null;
    }

    @Override
    public void sendAlarmMsg(BaseNotifyCtx ctx,NotifyItem notifyItem) {

    }

    @Override
    public String platform() {
        return null;
    }

    protected abstract String getNoticeTemplate();

    protected abstract String getAlarmTemplate();


    protected abstract Pair<String, String> getColors();


}
