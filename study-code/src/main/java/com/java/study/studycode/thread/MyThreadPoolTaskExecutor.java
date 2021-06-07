package com.java.study.studycode.thread;

import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author:wb-cgm503374
 * @Description 自定义线程执行器
 * 线程池异步调用，requestId传递
 * @Date:Created in 2021/3/18 上午12:03
 */
class MyThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

    @Override
    public void execute(Runnable task) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        super.execute(() -> run(task, contextMap));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(task);
    }

    private void run(Runnable runnable, Map<String, String> context) {
        if (context != null) {
            MDC.setContextMap(context);
        }
        try {
            runnable.run();
        } finally {
            MDC.clear();
        }
    }

    public static void main(String[] args) {
        ThreadPoolTaskExecutor executor = new MyThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("thread-");
        executor.initialize();

        MDC.put("requestId", "1111");

        executor.execute(() -> {
            System.out.print(Thread.currentThread().getName() + ":" + MDC.get("requestId"));
        });

        executor.shutdown();
    }

    @Test
    public void test2() {

        // 核心线程1，阻塞队列Integer.MAX_VALUE，阻塞队列过长，会OOM
        Executors.newSingleThreadExecutor();
        // 核心线程0，最大线程Integer.MAX_VALUE，线程会频繁销毁
        Executors.newCachedThreadPool();
        // 阻塞队列Integer.MAX_VALUE，阻塞队列过长，会OOM
        Executors.newFixedThreadPool(1);
        //
        Executors.newScheduledThreadPool(1);
    }
}
