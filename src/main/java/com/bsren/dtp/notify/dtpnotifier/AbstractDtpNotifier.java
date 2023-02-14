package com.bsren.dtp.notify.dtpnotifier;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.bsren.dtp.context.AlarmNotifyCtx;
import com.bsren.dtp.context.BaseNotifyCtx;
import com.bsren.dtp.context.DtpNotifyCtxHolder;
import com.bsren.dtp.dto.*;
import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.em.NotifyPlatformEnum;
import com.bsren.dtp.notify.alarm.AlarmCounter;
import com.bsren.dtp.notify.base.Notifier;
import com.bsren.dtp.thread.DtpExecutor;
import com.bsren.dtp.util.CommonUtil;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.bsren.dtp.constant.DynamicTpConst.UNKNOWN;
import static com.bsren.dtp.constant.LarkNotifyConst.*;
import static com.bsren.dtp.notify.manager.NotifyItemManager.getAlarmKeys;
import static com.bsren.dtp.notify.manager.NotifyItemManager.getAllAlarmKeys;

@Slf4j
public abstract class AbstractDtpNotifier implements DtpNotifier{

    private Notifier notifier;

    public AbstractDtpNotifier() { }

    public AbstractDtpNotifier(Notifier notifier) {
        this.notifier = notifier;
    }


    private String buildNoticeMsg(BaseNotifyCtx ctx, DtpMainProp oldProp, DtpMainProp newProp) {
        return null;
    }

    @Override
    public void sendChangeMsg(DtpMainProp oldProp, List<String> diffs) {
        NotifyPlatform platform = DtpNotifyCtxHolder.get().getPlatform(platform());
        String content = buildNoticeContent(platform, oldProp, diffs);
        if (StringUtils.isBlank(content)) {
            return;
        }
        notifier.send(platform, content);
    }

    @Override
    public void sendAlarmMsg(NotifyItemEnum notifyItemEnum) {
        NotifyPlatform platform = DtpNotifyCtxHolder.get().getPlatform(platform());
        String content = buildAlarmContent(platform, notifyItemEnum);
        if (StringUtils.isBlank(content)) {
            return;
        }
        notifier.send(platform, content);
    }

    protected String buildAlarmContent(NotifyPlatform platform, NotifyItemEnum notifyItemEnum) {
        AlarmNotifyCtx context = (AlarmNotifyCtx) DtpNotifyCtxHolder.get();
        String threadPoolName = context.getExecutorWrapper().getThreadPoolName();
        ExecutorWrapper executorWrapper = context.getExecutorWrapper();
        val executor = (ThreadPoolExecutor) context.getExecutorWrapper().getExecutor();
        NotifyItem notifyItem = context.getNotifyItem();
        AlarmInfo alarmInfo = context.getAlarmInfo();

        val alarmCounter = AlarmCounter.countStrRrq(threadPoolName, executor);
        String receivesStr = getReceives(platform.getPlatform(), platform.getReceivers());

        String content = String.format(
                getAlarmTemplate(),
                CommonUtil.getInstance().getServiceName(),
                CommonUtil.getInstance().getIp() + ":" + CommonUtil.getInstance().getPort(),
                CommonUtil.getInstance().getEnv(),
                populatePoolName(executorWrapper),
                notifyItemEnum.getValue(),
                notifyItem.getThreshold(),
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getPoolSize(),
                executor.getActiveCount(),
                executor.getLargestPoolSize(),
                executor.getTaskCount(),
                executor.getCompletedTaskCount(),
                executor.getQueue().size(),
                executor.getQueue().getClass().getSimpleName(),
                getQueueCapacity(executor),
                executor.getQueue().size(),
                executor.getQueue().remainingCapacity(),
                getRejectHandlerName(executor),
                alarmCounter.getLeft(),
                alarmCounter.getMiddle(),
                alarmCounter.getRight(),
                alarmInfo.getLastAlarmTime() == null ? UNKNOWN : alarmInfo.getLastAlarmTime(),
                DateUtil.now(),
                receivesStr,
                notifyItem.getInterval()
        );
        return highlightAlarmContent(content, notifyItemEnum);
    }


    protected abstract String getNoticeTemplate();

    protected abstract String getAlarmTemplate();

    protected abstract Pair<String, String> getColors();

    protected String buildNoticeContent(NotifyPlatform platform, DtpMainProp oldProp, List<String> diffs) {
        BaseNotifyCtx context = DtpNotifyCtxHolder.get();
        ExecutorWrapper executorWrapper = context.getExecutorWrapper();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorWrapper.getExecutor();
        String receivesStr = getReceives(platform.getPlatform(), platform.getReceivers());

        String content = String.format(
                getNoticeTemplate(),
                CommonUtil.getInstance().getServiceName(),
                CommonUtil.getInstance().getIp() + ":" + CommonUtil.getInstance().getPort(),
                CommonUtil.getInstance().getEnv(),
                populatePoolName(executorWrapper),
                oldProp.getCorePoolSize(),
                executor.getCorePoolSize(),
                oldProp.getMaxPoolSize(),
                executor.getMaximumPoolSize(),
                oldProp.isAllowCoreThreadTimeOut(),
                executor.allowsCoreThreadTimeOut(),
                oldProp.getKeepAliveTime(),
                executor.getKeepAliveTime(TimeUnit.SECONDS),
                executor.getQueue().getClass().getSimpleName(),
                oldProp.getQueueCapacity(),
                getQueueCapacity(executor),
                oldProp.getRejectType(),
                getRejectHandlerName(executor),
                receivesStr,
                DateTime.now()
        );
        return highlightNotifyContent(content, diffs);
    }

    private String getReceives(String platform, String receives) {
        if (StringUtils.isBlank(receives)) {
            return "";
        }
        if (NotifyPlatformEnum.LARK.name().toLowerCase().equals(platform)) {
            return Arrays.stream(receives.split(","))
                    .map(receive -> StringUtils.startsWith(receive, LARK_OPENID_PREFIX)
                            ? String.format(LARK_AT_FORMAT_OPENID, receive) : String.format(LARK_AT_FORMAT_USERNAME, receive))
                    .collect(Collectors.joining(" "));
        } else {
            String[] receivers = StringUtils.split(receives, ',');
            return Joiner.on(", @").join(receivers);
        }
    }

    protected String populatePoolName(ExecutorWrapper executorWrapper) {

        String poolAlisaName;
        if (executorWrapper.getExecutor() instanceof DtpExecutor) {
            poolAlisaName = ((DtpExecutor) executorWrapper.getExecutor()).getThreadPoolAliasName();
        } else {
            poolAlisaName = executorWrapper.getAlias();
        }
        if (StringUtils.isBlank(poolAlisaName)) {
            return executorWrapper.getThreadPoolName();
        }
        return executorWrapper.getThreadPoolName() + "(" + poolAlisaName + ")";
    }

    protected String getRejectHandlerName(ThreadPoolExecutor executor) {
        if (executor instanceof DtpExecutor) {
            return ((DtpExecutor) executor).getRejectHandlerName();
        }
        return executor.getRejectedExecutionHandler().getClass().getSimpleName();
    }

    protected int getQueueCapacity(ThreadPoolExecutor executor) {
        if (executor instanceof DtpExecutor) {
            return ((DtpExecutor) executor).getQueueCapacity();
        }
        return executor.getQueue().size() + executor.getQueue().remainingCapacity();
    }

    private String highlightNotifyContent(String content, List<String> diffs) {
        if (StringUtils.isBlank(content)) {
            return content;
        }

        Pair<String, String> pair = getColors();
        for (String field : diffs) {
            content = content.replace(field, pair.getLeft());
        }
        for (Field field : DtpMainProp.getMainProps()) {
            content = content.replace(field.getName(), pair.getRight());
        }
        return content;
    }

    private String highlightAlarmContent(String content, NotifyItemEnum notifyItemEnum) {
        if (StringUtils.isBlank(content)) {
            return content;
        }

        Set<String> colorKeys = getAlarmKeys(notifyItemEnum);
        Pair<String, String> pair = getColors();
        for (String field : colorKeys) {
            content = content.replace(field, pair.getLeft());
        }
        for (String field : getAllAlarmKeys()) {
            content = content.replace(field, pair.getRight());
        }
        return content;
    }
}
