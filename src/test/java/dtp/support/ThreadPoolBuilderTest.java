package dtp.support;

import com.bsren.dtp.support.ThreadPoolBuilder;
import org.junit.Test;

import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolBuilderTest {

    @Test
    public void test1(){

        ThreadPoolBuilder builder = ThreadPoolBuilder.newBuilder();
        ThreadPoolExecutor build = builder.corePoolSize(1)
                .threadPoolName("haha")
                .maximumPoolSize(2)
                .rejectedExecutionHandler("AbortPolicy")
                .queueCapacity(20).build();
        assert build.getCorePoolSize()==1;
        assert build.getMaximumPoolSize()==2;
    }

}
