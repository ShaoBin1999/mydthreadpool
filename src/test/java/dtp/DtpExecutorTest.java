package dtp;


import cn.hutool.core.thread.NamedThreadFactory;
import com.bsren.dtp.runnable.DtpRunnable;
import com.bsren.dtp.thread.DtpExecutor;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class DtpExecutorTest {


    @Test
    public void test1() throws InterruptedException {
        DtpExecutor dtpExecutor = new DtpExecutor(
                1,
                1,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5),
                new NamedThreadFactory("gaga",false));
        dtpExecutor.setThreadPoolName("dtp");
        dtpExecutor.setQueueTimeout(500);
        dtpExecutor.setRunTimeout(500);
        for (int i=0;i<6;i++){
            dtpExecutor.execute(new DtpRunnable(new Runnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    int i = random.nextInt(10);
                    try {
                        Thread.sleep(i*100);
                        System.out.println(Thread.currentThread().getName()+" sleep "+i*100+"ms");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            },"group","name"+i));
        }
        Thread.sleep(10000);
        dtpExecutor.shutdown();
    }

}