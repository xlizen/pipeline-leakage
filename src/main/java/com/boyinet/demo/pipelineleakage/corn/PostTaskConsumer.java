package com.boyinet.demo.pipelineleakage.corn;

import com.boyinet.demo.pipelineleakage.bean.Result;
import com.boyinet.demo.pipelineleakage.common.R;
import com.boyinet.demo.pipelineleakage.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Component()
@Slf4j
public class PostTaskConsumer {

    private final HttpUtil httpUtil;

    private final Executor executor;

    public static final DelayQueue<R<Result>> DELAY_QUEUE = new DelayQueue<>();

    public PostTaskConsumer(HttpUtil httpUtil, Executor executor) {
        this.httpUtil = httpUtil;
        this.executor = executor;
    }

    @PostConstruct
    public void listen() {
        executor.execute(() -> {
            while (true) {
                R<Result> take;
                try {
                    take = DELAY_QUEUE.take();
                    if (!httpUtil.postLeak(take)) {
                        long time = take.getTime();
                        if (time < 30) {
                            take.setTime(time * 2, TimeUnit.SECONDS);
                            DELAY_QUEUE.put(take);
                        } else {
                            log.info("[数据重发],延时时间为{}S,已重试3次，不再执行", time);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
