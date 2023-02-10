package com.bsren.dtp.notify.dtpnotifier;

import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.dto.DtpMainProp;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.em.NotifyItemEnum;

import java.util.List;

public interface DtpNotifier {

    String platform();

    void sendChangeMsg(BaseNotifyCtx ctx,DtpMainProp oldProp, DtpMainProp newProp);

    void sendAlarmMsg(BaseNotifyCtx ctx,NotifyItem notifyItem);
}
