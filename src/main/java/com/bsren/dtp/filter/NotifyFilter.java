package com.bsren.dtp.filter;

import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.em.NotifyTypeEnum;

public interface NotifyFilter extends Filter<BaseNotifyCtx> {

    default boolean supports(NotifyTypeEnum notifyType) {
        return true;
    }
}
