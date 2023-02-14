package com.bsren.dtp.adapter.brpc;

import com.baidu.cloud.starlight.api.common.URI;
import com.baidu.cloud.starlight.api.rpc.StarlightServer;
import com.baidu.cloud.starlight.api.rpc.threadpool.ThreadPoolFactory;
import com.baidu.cloud.starlight.api.transport.ServerPeer;
import com.baidu.cloud.starlight.core.rpc.DefaultStarlightServer;
import com.baidu.cloud.starlight.core.rpc.ServerProcessor;
import com.baidu.cloud.starlight.transport.netty.NettyServer;
import com.bsren.dtp.adapter.common.AbstractDtpAdapter;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class StarlightServerDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "brpcServerTp";

    private static final String URI_FIELD = "uri";

    private static final String SERVER_PEER_FIELD = "serverPeer";

    private static final String THREAD_POOL_FACTORY_FIELD = "threadPoolFactory";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getBrpcTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        super.initialize();

        StarlightServer bean = ApplicationContextHolder.getBean(StarlightServer.class);
        if (!(bean instanceof DefaultStarlightServer)) {
            return;
        }
        DefaultStarlightServer starlightServer = (DefaultStarlightServer) bean;
        URI uri = (URI) ReflectionUtil.getFieldValue(DefaultStarlightServer.class,
                URI_FIELD, starlightServer);
        ServerPeer serverPeer = (ServerPeer) ReflectionUtil.getFieldValue(DefaultStarlightServer.class,
                SERVER_PEER_FIELD, starlightServer);

        if (Objects.isNull(uri) || Objects.isNull(serverPeer) || !(serverPeer instanceof NettyServer)) {
            return;
        }
        ServerProcessor processor = (ServerProcessor) serverPeer.getProcessor();
        if (Objects.isNull(processor)) {
            return;
        }
        ThreadPoolFactory threadPoolFactory = (ThreadPoolFactory) ReflectionUtil.getFieldValue(ServerProcessor.class,
                THREAD_POOL_FACTORY_FIELD, processor);
        if (Objects.isNull(threadPoolFactory)) {
            return;
        }
        String bizThreadPoolName = uri.getParameter("biz_thread_pool_name") + "#server";
        ThreadPoolExecutor executor = threadPoolFactory.defaultThreadPool();
        if (Objects.nonNull(executor)) {
            ExecutorWrapper executorWrapper = new ExecutorWrapper(bizThreadPoolName, executor);
            initNotifyItems(bizThreadPoolName, executorWrapper);
            executors.put(bizThreadPoolName, executorWrapper);
        }
        log.info("DynamicTp adapter, brpc server executors init end, executors: {}", executors);
    }
}
