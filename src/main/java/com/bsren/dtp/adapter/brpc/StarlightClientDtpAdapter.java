package com.bsren.dtp.adapter.brpc;


import com.baidu.cloud.starlight.api.rpc.StarlightClient;
import com.baidu.cloud.starlight.api.rpc.threadpool.ThreadPoolFactory;
import com.baidu.cloud.starlight.core.rpc.SingleStarlightClient;
import com.baidu.cloud.starlight.springcloud.client.cluster.SingleStarlightClientManager;
import com.bsren.dtp.adapter.common.AbstractDtpAdapter;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.util.ReflectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * StarlightClientDtpAdapter related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
public class StarlightClientDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "brpcClientTp";

    private static final String THREAD_POOL_FIELD = "threadPoolOfAll";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getBrpcTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();

        SingleStarlightClientManager sscManager = null;
        Map<String, StarlightClient> scBeans = Maps.newHashMap();
        try {
            sscManager = ApplicationContextHolder.getBean(SingleStarlightClientManager.class);
            scBeans = ApplicationContextHolder.getBeansOfType(StarlightClient.class);
        } catch (Exception e) {
            log.warn("getBean error, msg: {}", e.getMessage());
        }

        List<StarlightClient> starlightClients = Lists.newArrayList();
        if (MapUtils.isNotEmpty(scBeans)) {
            starlightClients.addAll(scBeans.values());
        }
        if (Objects.nonNull(sscManager) && MapUtils.isNotEmpty(sscManager.allSingleClients())) {
            starlightClients.addAll(sscManager.allSingleClients().values());
        }
        if (CollectionUtils.isEmpty(starlightClients)) {
            log.warn("Cannot find beans of type StarlightClient.");
            return;
        }

        starlightClients.forEach(v -> {
            ThreadPoolFactory threadPoolFactory = (ThreadPoolFactory) ReflectionUtil.getFieldValue(SingleStarlightClient.class,
                    THREAD_POOL_FIELD, v);
            if (Objects.isNull(threadPoolFactory)) {
                return;
            }
            String bizThreadPoolName = v.remoteURI().getParameter("biz_thread_pool_name") + "#client";
            ThreadPoolExecutor executor = threadPoolFactory.defaultThreadPool();
            if (Objects.nonNull(executor)) {
                ExecutorWrapper executorWrapper = new ExecutorWrapper(bizThreadPoolName, executor);
                initNotifyItems(bizThreadPoolName, executorWrapper);
                executors.put(bizThreadPoolName, executorWrapper);
            }
        });
        log.info("DynamicTp adapter, brpc client executors init end, executors: {}", executors);
    }
}
