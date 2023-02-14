package com.bsren.dtp.filter;

import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.NotifyItem;
import com.bsren.dtp.notify.manager.NotifyItemManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Objects;

@Slf4j
public class NoticeBaseFilter implements NotifyFilter {

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void doFilter(BaseNotifyCtx context, Invoker<BaseNotifyCtx> nextInvoker) {

        ExecutorWrapper executorWrapper = context.getExecutorWrapper();
        NotifyItem notifyItem = NotifyItemManager.getNotifyItem(executorWrapper, context.getNotifyItemEnum());
        if (Objects.isNull(notifyItem) || !satisfyBaseCondition(notifyItem, executorWrapper)) {
            log.debug("DynamicTp refresh, change notification is not enabled, threadPoolName: {}",
                    executorWrapper.getThreadPoolName());
            return;
        }
        nextInvoker.invoke(context);
    }

    public boolean satisfyBaseCondition(NotifyItem notifyItem, ExecutorWrapper executor) {
        return executor.isNotifyEnabled()
                && notifyItem.isEnabled()
                && CollectionUtils.isNotEmpty(notifyItem.getPlatforms());
    }
}
