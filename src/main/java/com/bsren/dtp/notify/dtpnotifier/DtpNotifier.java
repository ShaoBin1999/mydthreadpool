package com.bsren.dtp.notify.dtpnotifier;

import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.dto.DtpMainProp;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.em.NotifyItemEnum;

import java.util.List;

public interface DtpNotifier {

    String platform();

    void sendChangeMsg(DtpMainProp oldProp, List<String> diffs);

    void sendAlarmMsg(NotifyItemEnum notifyItemEnum);
}
