package com.bsren.dtp.filter;

import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.context.DtpNotifyCtxHolder;
import com.bsren.dtp.context.NoticeNotifyCtx;
import com.bsren.dtp.handler.NotifierHandler;

public class NoticeInvoker implements Invoker<BaseNotifyCtx> {

    @Override
    public void invoke(BaseNotifyCtx context) {
        DtpNotifyCtxHolder.set(context);
        NoticeNotifyCtx noticeCtx = (NoticeNotifyCtx) context;
        NotifierHandler.getInstance().sendNotice(noticeCtx.getProp(), noticeCtx.getDiffs());
    }
}
