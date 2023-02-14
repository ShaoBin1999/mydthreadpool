package com.bsren.dtp.context;

import com.bsren.dtp.dto.DtpMainProp;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.dto.NotifyPlatform;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeNotifyCtx extends BaseNotifyCtx{

    private DtpMainProp prop;

    private List<String> diffs;

    public NoticeNotifyCtx(ExecutorWrapper wrapper, NotifyItem notifyItem) {
        super(wrapper, notifyItem);
    }

    public NoticeNotifyCtx(ExecutorWrapper wrapper, NotifyItem notifyItem, List<NotifyPlatform> platforms,
                     DtpMainProp prop, List<String> diffs) {
        super(wrapper, notifyItem);
        setPlatforms(platforms);
        this.prop = prop;
        this.diffs = diffs;
    }
}
