package com.bsren.dtp.adapter.common;

import com.bsren.dtp.context.NoticeNotifyCtx;
import com.bsren.dtp.convert.ExecutorConverter;
import com.bsren.dtp.convert.MetricsConverter;
import com.bsren.dtp.dto.*;
import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.notify.manager.AlarmManager;
import com.bsren.dtp.notify.manager.NoticeManager;
import com.bsren.dtp.notify.manager.NotifyItemManager;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.properties.SimpleTpProperties;
import com.bsren.dtp.util.StreamUtil;
import com.github.dadiyang.equator.Equator;
import com.github.dadiyang.equator.FieldInfo;
import com.github.dadiyang.equator.GetterBaseEquator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static com.bsren.dtp.constant.DynamicTpConst.PROPERTIES_CHANGE_SHOW_STYLE;
import static com.bsren.dtp.dto.NotifyItem.mergeSimpleNotifyItems;
import static java.util.stream.Collectors.toList;

@Slf4j
public abstract class AbstractDtpAdapter implements DtpAdapter, ApplicationListener<ApplicationReadyEvent> {

    private static final Equator EQUATOR = new GetterBaseEquator();

    protected final Map<String, ExecutorWrapper> executors = Maps.newHashMap();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            DtpProperties dtpProperties = ApplicationContextHolder.getBean(DtpProperties.class);
            initialize();
            refresh(dtpProperties);
        } catch (Exception e) {
            log.error("Init third party thread pool failed.", e);
        }
    }

    protected void initialize() { }

    public void register(String poolName, ThreadPoolExecutor threadPoolExecutor) { }

    @Override
    public Map<String, ExecutorWrapper> getExecutorWrappers() {
        return executors;
    }

    /**
     * Get multi thread pool stats.
     *
     * @return thead pools stats
     */
    @Override
    public List<ThreadPoolStats> getMultiPoolStats() {
        val executorWrappers = getExecutorWrappers();
        if (MapUtils.isEmpty(executorWrappers)) {
            return Collections.emptyList();
        }

        List<ThreadPoolStats> threadPoolStats = Lists.newArrayList();
        executorWrappers.forEach((k, v) -> threadPoolStats.add(MetricsConverter.convert(v)));
        return threadPoolStats;
    }

    public void initNotifyItems(String poolName, ExecutorWrapper executorWrapper) {
        AlarmManager.initAlarm(poolName, executorWrapper.getNotifyItems());
    }

    public void refresh(String name, List<SimpleTpProperties> properties, List<NotifyPlatform> platforms) {
        Map<String,ExecutorWrapper> executorWrappers = getExecutorWrappers();
        if (CollectionUtils.isEmpty(properties) || MapUtils.isEmpty(executorWrappers)) {
            return;
        }

        Map<String,SimpleTpProperties> tmpMap = StreamUtil.toMap(properties, SimpleTpProperties::getThreadPoolName);
        executorWrappers.forEach((k, v) -> refresh(name, v, platforms, tmpMap.get(k)));
    }


    /**
     * 线程池配置和通知平台均可能发生变化
     * 所以在才会判断old 和 new一样不一样
     * 之后记日记，并异步发送通知
     */
    public void refresh(String name,
                        ExecutorWrapper executorWrapper,
                        List<NotifyPlatform> platforms,
                        SimpleTpProperties properties) {

        if (Objects.isNull(properties) || Objects.isNull(executorWrapper) || containsInvalidParams(properties, log)) {
            return;
        }

        DtpMainProp oldProp = ExecutorConverter.convert(executorWrapper);
        doRefresh(executorWrapper, platforms, properties);
        DtpMainProp newProp = ExecutorConverter.convert(executorWrapper);
        if (oldProp.equals(newProp)) {
            log.warn("DynamicTp adapter refresh, main properties of [{}] have not changed.",
                    executorWrapper.getThreadPoolName());
            return;
        }

        List<FieldInfo> diffFields = EQUATOR.getDiffFields(oldProp, newProp);
        List<String> diffKeys = diffFields.stream().map(FieldInfo::getFieldName).collect(toList());
        log.info("DynamicTp {} adapter, [{}] refreshed end, changed keys: {}, corePoolSize: [{}], "
                        + "maxPoolSize: [{}], keepAliveTime: [{}]",
                name,
                executorWrapper.getThreadPoolName(),
                diffKeys,
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getCorePoolSize(), newProp.getCorePoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getMaxPoolSize(), newProp.getMaxPoolSize()),
                String.format(PROPERTIES_CHANGE_SHOW_STYLE, oldProp.getKeepAliveTime(), newProp.getKeepAliveTime()));

        NotifyItem notifyItem = NotifyItemManager.getNotifyItem(executorWrapper, NotifyItemEnum.CONFIG_CHANGE);
        NoticeNotifyCtx context = new NoticeNotifyCtx(executorWrapper, notifyItem, platforms, oldProp, diffKeys);
        NoticeManager.doNoticeAsync(context);
    }



    /**
     * 1. 设置线程数
     * 2. 设置线程存活时间
     * 3. 设置线程池别名
     * 4. 给定通知项，必须包装基本的通知项也在里面
     * 5. 设置是否通知
     * 6. 触发alarmManager.refreshAlarm方法，初始化alarmLimit和alarmCounter
     */
    private void doRefresh(ExecutorWrapper executorWrapper,
                           List<NotifyPlatform> platforms,
                           SimpleTpProperties properties) {

        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        doRefreshPoolSize(executor, properties);

        if (!Objects.equals(executor.getKeepAliveTime(properties.getUnit()), properties.getKeepAliveTime())) {
            executor.setKeepAliveTime(properties.getKeepAliveTime(), properties.getUnit());
        }
        if (StringUtils.isNotBlank(properties.getThreadPoolAliasName())) {
            executorWrapper.setAlias(properties.getThreadPoolAliasName());
        }
        List<NotifyItem> allNotifyItems = mergeSimpleNotifyItems(properties.getNotifyItems());
        AlarmManager.refreshAlarm(executorWrapper.getThreadPoolName(), platforms,
                executorWrapper.getNotifyItems(), allNotifyItems);
        executorWrapper.setNotifyItems(allNotifyItems);
        executorWrapper.setNotifyEnabled(properties.isNotifyEnabled());
    }

    /**
     * 设置max和core线程数
     */
    private void doRefreshPoolSize(ThreadPoolExecutor executor, SimpleTpProperties properties) {
        if (properties.getMaximumPoolSize() >= executor.getMaximumPoolSize()) {
            if (!Objects.equals(properties.getMaximumPoolSize(), executor.getMaximumPoolSize())) {
                executor.setMaximumPoolSize(properties.getMaximumPoolSize());
            }
            if (!Objects.equals(properties.getCorePoolSize(), executor.getCorePoolSize())) {
                executor.setCorePoolSize(properties.getCorePoolSize());
            }
            return;
        }

        if (!Objects.equals(properties.getCorePoolSize(), executor.getCorePoolSize())) {
            executor.setCorePoolSize(properties.getCorePoolSize());
        }
        if (!Objects.equals(properties.getMaximumPoolSize(), executor.getMaximumPoolSize())) {
            executor.setMaximumPoolSize(properties.getMaximumPoolSize());
        }
    }
}