package com.bsren.dtp.context;

import com.bsren.dtp.dto.DtpMainProp;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.NotifyItem;
import lombok.Data;

import java.util.List;

@Data
public class NoticeNotifyItem extends BaseNotifyCtx{

    private DtpMainProp oldProp;

    private DtpMainProp newProp;

    public NoticeNotifyItem(ExecutorWrapper wrapper, NotifyItem notifyItem) {
        super(wrapper, notifyItem);
    }

    public NoticeNotifyItem(ExecutorWrapper wrapper, NotifyItem notifyItem,
                            DtpMainProp prop, DtpMainProp newProp) {
        super(wrapper, notifyItem);
        this.oldProp = prop;
        this.newProp = newProp;
    }
}
