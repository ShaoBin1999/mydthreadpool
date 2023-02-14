package com.bsren.dtp.context;

public class DtpNotifyCtxHolder {

    private static final ThreadLocal<BaseNotifyCtx> CONTEXT = new ThreadLocal<>();

    private DtpNotifyCtxHolder() { }

    public static void set(BaseNotifyCtx dtpContext) {
        CONTEXT.set(dtpContext);
    }

    public static BaseNotifyCtx get() {
        return CONTEXT.get();
    }

    public static void remove() {
        CONTEXT.remove();
    }

}
