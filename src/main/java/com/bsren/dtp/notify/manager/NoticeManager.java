package com.bsren.dtp.notify.manager;

import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.context.NoticeNotifyCtx;
import com.bsren.dtp.filter.InvokerChain;
import com.bsren.dtp.support.ThreadPoolCreator;

import java.util.concurrent.ExecutorService;

public class NoticeManager {


    private static final ExecutorService NOTICE_EXECUTOR = ThreadPoolCreator.createCommonFast("dtp-notify");

    private NoticeManager() { }

    private static final InvokerChain<BaseNotifyCtx> NOTICE_INVOKER_CHAIN;

    static {
        NOTICE_INVOKER_CHAIN = NotifyFilterBuilder.getCommonInvokerChain();
    }

    public static void doNotice(NoticeNotifyCtx noticeCtx) {
        NOTICE_INVOKER_CHAIN.proceed(noticeCtx);
    }

    public static void doNoticeAsync(NoticeNotifyCtx noticeCtx) {
        NOTICE_EXECUTOR.execute(() -> doNotice(noticeCtx));
    }
}
