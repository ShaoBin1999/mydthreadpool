package com.bsren.dtp.adapter.rocketmq;

import com.alibaba.cloud.stream.binder.rocketmq.integration.RocketMQInboundChannelAdapter;
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
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binding.BindingService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * StreamRocketMqDtpAdapter related
 *
 * @author zhanbj
 * @since 1.1.0
 */
@Slf4j
@SuppressWarnings("all")
public class StreamRocketMqDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "rocketMqTp";

    private static final String CONSUME_EXECUTOR_FIELD_NAME = "consumeExecutor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getRocketMqTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();

        val beans = ApplicationContextHolder.getBeansOfType(BindingService.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type BindingService.");
            return;
        }

        beans.forEach((bindingServiceName, bindingService) -> {
            val consumerBindings = (Map<String, List<Binding<?>>>) ReflectionUtil.getFieldValue(BindingService.class,
                    "consumerBindings", bindingService);
            if (MapUtils.isEmpty(consumerBindings)) {
                return;
            }
            consumerBindings.forEach((bindingName, messageChannelBinders) -> {
                val messageChannelBinder = messageChannelBinders.get(0);
                Class<?> messageChannelBinderClass = messageChannelBinder.getClass();
                val lifecycle = (RocketMQInboundChannelAdapter) ReflectionUtil.getFieldValue(messageChannelBinderClass,
                        "lifecycle", messageChannelBinder);
                val pushConsumer = (DefaultMQPushConsumer) ReflectionUtil.getFieldValue(RocketMQInboundChannelAdapter.class,
                        "pushConsumer", lifecycle);
                val defaultMQPushConsumerImpl = (DefaultMQPushConsumerImpl) ReflectionUtil.getFieldValue(DefaultMQPushConsumer.class,
                        "defaultMQPushConsumerImpl", pushConsumer);
                if (defaultMQPushConsumerImpl == null || defaultMQPushConsumerImpl.getConsumeMessageService() == null) {
                    return;
                }
                val consumeMessageService = defaultMQPushConsumerImpl.getConsumeMessageService();
                ThreadPoolExecutor executor = null;
                if (consumeMessageService instanceof ConsumeMessageConcurrentlyService) {
                    executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(ConsumeMessageConcurrentlyService.class,
                            CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
                } else if (consumeMessageService instanceof ConsumeMessageOrderlyService) {
                    executor = (ThreadPoolExecutor) ReflectionUtil.getFieldValue(ConsumeMessageOrderlyService.class,
                            CONSUME_EXECUTOR_FIELD_NAME, consumeMessageService);
                }
                if (Objects.nonNull(executor)) {
                    val destination = (String) ReflectionUtil.getFieldValue(messageChannelBinderClass,
                            "name", messageChannelBinder);
                    val group = (String) ReflectionUtil.getFieldValue(messageChannelBinderClass,
                            "group", messageChannelBinder);
                    String key = group + "#" + destination;
                    val executorWrapper = new ExecutorWrapper(key, executor);
                    initNotifyItems(key, executorWrapper);
                    executors.put(key, executorWrapper);
                }
            });
        });
        log.info("DynamicTp adapter, rocketMq consumer executors init end, executors: {}", executors);
    }
}
