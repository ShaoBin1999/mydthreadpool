package com.bsren.dtp.adapter.grpc;


import com.bsren.dtp.adapter.common.AbstractDtpAdapter;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.properties.DtpProperties;
import com.bsren.dtp.util.ReflectionUtil;
import io.grpc.internal.ServerImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.server.serverfactory.GrpcServerLifecycle;
import org.apache.commons.collections.MapUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * GrpcDtpAdapter related
 *
 * @author yanhom
 * @since 1.0.9
 */
@Slf4j
public class GrpcDtpAdapter extends AbstractDtpAdapter {

    private static final String NAME = "grpcTp";

    private static final String SERVER_FIELD = "server";

    private static final String EXECUTOR_FIELD = "executor";

    @Override
    public void refresh(DtpProperties dtpProperties) {
        refresh(NAME, dtpProperties.getGrpcTp(), dtpProperties.getPlatforms());
    }

    @Override
    protected void initialize() {
        Map<String,GrpcServerLifecycle> beans = ApplicationContextHolder.getBeansOfType(GrpcServerLifecycle.class);
        if (MapUtils.isEmpty(beans)) {
            log.warn("Cannot find beans of type GrpcServerLifecycle.");
            return;
        }
        beans.forEach((k, v) -> {
            Object server = ReflectionUtil.getFieldValue(GrpcServerLifecycle.class, SERVER_FIELD, v);
            if (Objects.isNull(server)) {
                return;
            }
            ServerImpl serverImpl = (ServerImpl) server;
            Executor executor = (Executor) ReflectionUtil.getFieldValue(ServerImpl.class, EXECUTOR_FIELD, serverImpl);
            String tpName = genTpName(k);
            if (Objects.nonNull(executor)) {
                ExecutorWrapper executorWrapper = new ExecutorWrapper(tpName, executor);
                initNotifyItems(tpName, executorWrapper);
                executors.put(tpName, executorWrapper);
            }
        });
        log.info("DynamicTp adapter, grpc server executors init end, executors: {}", executors);
    }

    /**
     * Gen tp name.
     *
     * @param serverLifeCycleName (shadedNettyGrpcServerLifecycle / inProcessGrpcServerLifecycle / nettyGrpcServerLifecycle)
     * @return tp name
     */
    private String genTpName(String serverLifeCycleName) {
        return serverLifeCycleName.replace("GrpcServerLifecycle", "Tp");
    }
}
