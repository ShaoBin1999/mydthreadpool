package com.bsren.dtp.adapter.rocketmq;

import com.bsren.dtp.adapter.common.AbstractDtpAdapter;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.MapUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService;
import org.apache.rocketmq.client.impl.consumer.ConsumeMessageOrderlyService;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RocketMqDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.6
 */
@SuppressWarnings("all")
@Slf4j
public class RocketMqDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "rocketMqTp";

    private static final String CONSUME_EXECUTOR_FIELD_NAME = "consumeExecutor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getRocketMqTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();

        val beans = ApplicationContextHolder.getBeansOfType(DefaultRocketMQListenerContainer.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type DefaultRocketMQListenerContainer.");
            return;
        }
        beans.forEach((k, v) -> {
            DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) v;
            DefaultMQPushConsumer consumer = container.getConsumer();
            val pushConsumer = (DefaultMQPushConsumerImpl) ReflectionUtil.getFieldValue(DefaultMQPushConsumer.class,
                    "defaultMQPushConsumerImpl", consumer);
            if (Objects.isNull(pushConsumer)) {
                return;
            }

            String key = container.getConsumerGroup() + "#" + container.getTopic();
            ThreadPoolExecutor executor = null;
            val consumeMessageService = pushConsumer.getConsumeMessageService();
            if (consumeMessageService instanceof ConsumeMessageConcurrentlyService) {
                executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(ConsumeMessageConcurrentlyService.class,
                        CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
            } else if (consumeMessageService instanceof ConsumeMessageOrderlyService) {
                executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(ConsumeMessageOrderlyService.class,
                        CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
            }
            if (Objects.nonNull(executor)) {
                val executorWrapper = new ExecutorWrapper(key, executor);
                initNotifyItems(key, executorWrapper);
                executors.put(key, executorWrapper);
            }
        });
        log.info("DynamicTp adapter, rocketMq consumer executors init end, executors: {}", executors);
    }
}
