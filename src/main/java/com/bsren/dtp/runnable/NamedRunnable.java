package com.bsren.dtp.runnable;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
/**
 * 为runnable对象包装名称，应用于统计不同分组的runnable执行情况
 */
@Data
@AllArgsConstructor
public class NamedRunnable implements Runnable{

    Runnable runnable;

    String name;

    @Override
    public void run() {
        this.runnable.run();
    }

    public static NamedRunnable of(Runnable runnable, String name) {
        if (StringUtils.isBlank(name)) {
            name = runnable.getClass().getSimpleName() + "-" + UUID.randomUUID();
        }
        return new NamedRunnable(runnable, name);
    }
}
