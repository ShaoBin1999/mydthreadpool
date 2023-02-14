package com.bsren.dtp.notify.dtpnotifier;


import com.bsren.dtp.constant.DingNotifyConst;
import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.em.NotifyPlatformEnum;
import com.bsren.dtp.notify.base.Notifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static com.bsren.dtp.constant.DingNotifyConst.DING_ALARM_TEMPLATE;
import static com.bsren.dtp.constant.DingNotifyConst.DING_CHANGE_NOTICE_TEMPLATE;


/**
 * DtpDingNotifier related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpDingNotifier extends AbstractDtpNotifier {

    public DtpDingNotifier(Notifier notifier) {
        super(notifier);
    }

    @Override
    public String platform() {
        return NotifyPlatformEnum.DING.name().toLowerCase();
    }

    @Override
    protected String getNoticeTemplate() {
        return DING_CHANGE_NOTICE_TEMPLATE;
    }

    @Override
    protected String getAlarmTemplate() {
        return DING_ALARM_TEMPLATE;
    }

    @Override
    protected Pair<String, String> getColors() {
        return new ImmutablePair<>(DingNotifyConst.WARNING_COLOR, DingNotifyConst.CONTENT_COLOR);
    }
}
