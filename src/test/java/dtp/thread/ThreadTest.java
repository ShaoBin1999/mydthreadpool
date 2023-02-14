package dtp.thread;

import cn.hutool.core.thread.NamedThreadFactory;
import com.bsren.dtp.queue.TaskQueue;
import com.bsren.dtp.runnable.DtpRunnable;
import com.bsren.dtp.thread.EagerDtpExecutor;
import com.bsren.dtp.thread.OrderedDtpExecutor;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadTest {

    /**
     * test taskQueue and eagerDtpExecutor
     */
    @Test
    public void taskQueueTest() throws InterruptedException {
        TaskQueue taskQueue = new TaskQueue(10);
        EagerDtpExecutor executor = new EagerDtpExecutor(
                1,
                5,
                100,
                TimeUnit.SECONDS,
                taskQueue,
                new NamedThreadFactory("a",false),
                new ThreadPoolExecutor.DiscardPolicy()
        );
        taskQueue.setExecutor(executor);
        for (int i=0;i<5;i++){
            executor.execute(new DtpRunnable(new Task(),"group","name"+i));
        }
        while (executor.getActiveCount()!=0){
            System.out.println(executor.getActiveCount());
            Thread.sleep(500);
        }
    }

    /**
     * task has same args should be executed by the same thread
     */
    @Test
    public void orderedExecuteTest() throws InterruptedException {
        OrderedDtpExecutor executor = new OrderedDtpExecutor(
                3,
                5,
                100,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                new NamedThreadFactory("ha",true),
                new ThreadPoolExecutor.DiscardPolicy()
        );
        // must execute by a thread
        for (int i=0;i<5;i++){
            executor.execute("1",new DtpRunnable(new Task(),"group","name"+i));
        }
        Thread.sleep(10000);
    }


    /**
     * 利用threadFactory捕获异常
     */
    @Test
    public void NamedThreadFactoryTest() throws InterruptedException {
        com.bsren.dtp.thread.NamedThreadFactory threadFactory = new com.bsren.dtp.thread.NamedThreadFactory(
                "rsb",
                true,
                Thread.NORM_PRIORITY
        );
        Executor executor = new ThreadPoolExecutor(
                1,2,10,TimeUnit.SECONDS,new LinkedBlockingQueue<>(10),
                threadFactory,new ThreadPoolExecutor.DiscardPolicy()
        );

        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread());
                    System.out.println(1/0);
                }
            });
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        Thread.sleep(1000);
    }



}

class Task implements Runnable{
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
}
