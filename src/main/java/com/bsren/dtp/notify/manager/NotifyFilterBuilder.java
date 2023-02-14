package com.bsren.dtp.notify.manager;

import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.em.NotifyTypeEnum;
import com.bsren.dtp.filter.*;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class NotifyFilterBuilder {


    private NotifyFilterBuilder() { }

    public static InvokerChain<BaseNotifyCtx> getAlarmInvokerChain() {
        Map<String,NotifyFilter> filters = ApplicationContextHolder.getBeansOfType(NotifyFilter.class);
        Collection<NotifyFilter> alarmFilters = Lists.newArrayList(filters.values());
        alarmFilters.add(new AlarmBaseFilter());
        alarmFilters = alarmFilters.stream()
                .filter(x -> x.supports(NotifyTypeEnum.ALARM))
                .sorted(Comparator.comparing(Filter::getOrder))
                .collect(Collectors.toList());
        return InvokerChainFactory.buildInvokerChain(new AlarmInvoker(), alarmFilters.toArray(new NotifyFilter[0]));
    }

    public static InvokerChain<BaseNotifyCtx> getCommonInvokerChain() {
        Map<String,NotifyFilter> filters = ApplicationContextHolder.getBeansOfType(NotifyFilter.class);
        Collection<NotifyFilter> noticeFilters = Lists.newArrayList(filters.values());
        noticeFilters.add(new NoticeBaseFilter());
        noticeFilters = noticeFilters.stream()
                .filter(x -> x.supports(NotifyTypeEnum.COMMON))
                .sorted(Comparator.comparing(Filter::getOrder))
                .collect(Collectors.toList());
        return InvokerChainFactory.buildInvokerChain(new NoticeInvoker(), noticeFilters.toArray(new NotifyFilter[0]));
    }


}
