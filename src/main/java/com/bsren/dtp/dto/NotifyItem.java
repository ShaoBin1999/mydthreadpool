package com.bsren.dtp.dto;

import com.bsren.dtp.em.NotifyItemEnum;
import com.bsren.dtp.util.StringUtil;
import lombok.Data;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Data
public class NotifyItem {

    private boolean enabled = true;

    private String type;

    //超过阈值触发报警
    private int threshold;

    //报警时间间隔
    private int interval = 120;

    private List<String> platforms;

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    /**
     * 添加三个基本的notify
     * change, live的警报阈值为70 ,capacity的警报阈值为70
     */
    public static List<NotifyItem> getSimpleNotifyItems() {
        NotifyItem changeNotify = new NotifyItem();
        changeNotify.setType(NotifyItemEnum.CONFIG_CHANGE.getValue());

        NotifyItem liveNotify = new NotifyItem();
        liveNotify.setType(NotifyItemEnum.LIVE.getValue());
        liveNotify.setThreshold(70);

        NotifyItem capacityNotify = new NotifyItem();
        capacityNotify.setType(NotifyItemEnum.CAPACITY.getValue());
        capacityNotify.setThreshold(70);

        List<NotifyItem> notifyItems = new ArrayList<>(3);
        notifyItems.add(liveNotify);
        notifyItems.add(changeNotify);
        notifyItems.add(capacityNotify);

        return notifyItems;
    }


    /**
     * TODO 这里阈值建议改成可配置的
     * 添加所有的notify，在change,live,capacity的基础上加上reject,run_timeout,queue_timeout
     * reject,run_timeout,queue_timeout只要触发一次就会报警
     */
    public static List<NotifyItem> getAllNotifyItems() {

        NotifyItem rejectNotify = new NotifyItem();
        rejectNotify.setType(NotifyItemEnum.REJECT.getValue());
        rejectNotify.setThreshold(1);

        NotifyItem runTimeoutNotify = new NotifyItem();
        runTimeoutNotify.setType(NotifyItemEnum.RUN_TIMEOUT.getValue());
        runTimeoutNotify.setThreshold(1);

        NotifyItem queueTimeoutNotify = new NotifyItem();
        queueTimeoutNotify.setType(NotifyItemEnum.QUEUE_TIMEOUT.getValue());
        queueTimeoutNotify.setThreshold(1);

        List<NotifyItem> notifyItems = new ArrayList<>(6);
        notifyItems.addAll(getSimpleNotifyItems());
        notifyItems.add(rejectNotify);
        notifyItems.add(runTimeoutNotify);
        notifyItems.add(queueTimeoutNotify);

        return notifyItems;
    }


    public static List<NotifyItem> mergeAllNotifyItems(List<NotifyItem> source) {
        // update notify items
        if (CollectionUtils.isEmpty(source)) {
            return getAllNotifyItems();
        } else {
            val configuredTypes = source.stream().map(NotifyItem::getType).collect(toList());
            val defaultItems = getAllNotifyItems().stream()
                    .filter(t -> !StringUtil.containsIgnoreCase(t.getType(), configuredTypes))
                    .collect(Collectors.toList());
            source.addAll(defaultItems);
            return source;
        }
    }

    public static List<NotifyItem> mergeSimpleNotifyItems(List<NotifyItem> source) {
        // update notify items
        if (CollectionUtils.isEmpty(source)) {
            return getSimpleNotifyItems();
        } else {
            List<String> configuredTypes = source.stream().map(NotifyItem::getType).collect(toList());
            List<NotifyItem> defaultItems = getSimpleNotifyItems().stream()
                    .filter(t -> !StringUtil.containsIgnoreCase(t.getType(), configuredTypes))
                    .collect(Collectors.toList());
            source.addAll(defaultItems);
            return source;
        }
    }
}
