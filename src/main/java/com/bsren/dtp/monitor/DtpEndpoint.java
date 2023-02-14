package com.bsren.dtp.monitor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.RuntimeInfo;
import com.bsren.dtp.registry.DtpRegistry;
import com.bsren.dtp.convert.MetricsConverter;
import com.bsren.dtp.dto.ExecutorWrapper;
import com.bsren.dtp.dto.JvmStats;
import com.bsren.dtp.dto.Metrics;
import com.bsren.dtp.holder.ApplicationContextHolder;
import com.bsren.dtp.support.MetricsAware;
import com.bsren.dtp.thread.DtpExecutor;
import com.google.common.collect.Lists;
import lombok.val;
import org.apache.commons.collections.MapUtils;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * DtpEndpoint related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Endpoint(id = "dynamic-tp")
public class DtpEndpoint {

    @ReadOperation
    public List<Metrics> invoke() {

        List<Metrics> metricsList = Lists.newArrayList();
        List<String> dtpNames = DtpRegistry.listAllDtpNames();
        dtpNames.forEach(x -> {
            DtpExecutor executor = DtpRegistry.getDtpExecutor(x);
            metricsList.add(MetricsConverter.convert(executor));
        });

        List<String> commonNames = DtpRegistry.listAllCommonNames();
        commonNames.forEach(x -> {
            ExecutorWrapper wrapper = DtpRegistry.getCommonExecutor(x);
            metricsList.add(MetricsConverter.convert(wrapper));
        });

        val handlerMap = ApplicationContextHolder.getBeansOfType(MetricsAware.class);
        if (MapUtils.isNotEmpty(handlerMap)) {
            handlerMap.forEach((k, v) -> metricsList.addAll(v.getMultiPoolStats()));
        }

        JvmStats jvmStats = new JvmStats();
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        jvmStats.setMaxMemory(FileUtil.readableFileSize(runtimeInfo.getMaxMemory()));
        jvmStats.setTotalMemory(FileUtil.readableFileSize(runtimeInfo.getTotalMemory()));
        jvmStats.setFreeMemory(FileUtil.readableFileSize(runtimeInfo.getFreeMemory()));
        jvmStats.setUsableMemory(FileUtil.readableFileSize(runtimeInfo.getUsableMemory()));
        metricsList.add(jvmStats);
        return metricsList;
    }
}
