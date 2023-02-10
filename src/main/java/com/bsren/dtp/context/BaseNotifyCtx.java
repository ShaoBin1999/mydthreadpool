package com.bsren.dtp.context;

import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.dto.NotifyPlatform;
import com.bsren.dtp.em.NotifyItemEnum;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

@Data
public class BaseNotifyCtx {

    private ExecutorWrapper executorWrapper;

    private List<NotifyPlatform> platforms;

    private NotifyItem notifyItem;

    public BaseNotifyCtx() { }

    public BaseNotifyCtx(ExecutorWrapper wrapper, NotifyItem notifyItem) {
        this.executorWrapper = wrapper;
        this.notifyItem = notifyItem;
    }


    public NotifyPlatform getPlatform(String platform) {
        if (CollectionUtils.isEmpty(platforms)) {
            return null;
        }
        return platforms.stream().filter(p -> p.getPlatform().equalsIgnoreCase(platform))
                .findFirst().orElse(null);
    }

    public NotifyItemEnum getNotifyItemEnum() {
        return NotifyItemEnum.of(notifyItem.getType());
    }
}
