package com.java.study.studycode.disruptor;

import com.example.dailypractice.disruptor.obj.LongObj;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @Author:wb-cgm503374
 * @Description <p>
 * Disruptor 的事件发布过程是一个两阶段提交的过程：
 * 　　第一步：先从 RingBuffer 获取下一个可以写入的事件的序号；
 * 　　第二步：获取对应的事件对象，将数据写入事件对象；
 * 　　第三部：将事件提交到 RingBuffer;
 * 事件只有在提交之后才会通知消息消费者进行处理；
 * </p>
 * 链接：https://www.jianshu.com/p/e46b14b766f2
 * @Date:Created in 2021/3/14 下午5:40
 */

public class DisruptorMain {
    private final static int BUFFER_SIZE = 2 * 1024;

    public static void main(String[] args) throws InterruptedException {
//        SpringApplication.run(DailyPracticeApplication.class, args);
        long l = System.currentTimeMillis();
        signal();
        System.out.println(System.currentTimeMillis() - l);

        // multi
//        multi();
    }

    // 单生产者单消费者
    private static void signal() throws InterruptedException {
        ExecutorService producerPool = Executors.newCachedThreadPool();
        // 创建 Disruptor
        Disruptor<LongObj> disruptor = new Disruptor<>(new DisruptorEventFactory(), BUFFER_SIZE,
                (ThreadFactory) Thread::new, ProducerType.MULTI, new YieldingWaitStrategy());

        CountDownLatch latch = new CountDownLatch(1);
        // 消费者
        DisruptorEventSignalStartCustomer startCustomer = new DisruptorEventSignalStartCustomer();
        DisruptorEventSignalCustomer customer = new DisruptorEventSignalCustomer(latch);
        // 链接消费者事件 关联多个消费者事件并定义处理顺序
        disruptor.handleEventsWith(startCustomer).then(customer);
        // 启动
        disruptor.start();

        // 生产者开始生产数据
        RingBuffer<LongObj> ringBuffer = disruptor.getRingBuffer();
        producerPool.submit(new DisruptorProducer(ringBuffer));


        latch.await();
        System.out.println("customer count:" + customer.getCount());
        System.out.println("DisruptorEventFactory count:" + DisruptorEventFactory.getaCount());
        disruptor.shutdown();
        producerPool.shutdown();
    }

    // 多生产者多消费者
    private static void multi() {
        //线程数
        int processor = Runtime.getRuntime().availableProcessors() * 2;
        //构造消费者一个线程池， 实际项目中最好不要用Executors来构建
        ExecutorService consumerExecutor = Executors.newFixedThreadPool(processor);
        //构造生产者线程池
        ExecutorService produceerExecutor = Executors.newFixedThreadPool(processor);
        //定义一个ringBuffer,也就是相当于一个队列
        RingBuffer ringBuffer = RingBuffer.create(ProducerType.MULTI, LongObj::new, BUFFER_SIZE, new YieldingWaitStrategy());
        //定义一个消费者池，
        DisruptorEventMultiCustomer[] consumers = new DisruptorEventMultiCustomer[processor];
        for (int i = 0; i < processor; i++) {
            consumers[i] = new DisruptorEventMultiCustomer();
        }
        WorkerPool workerPool = new WorkerPool<LongObj>(ringBuffer,
                ringBuffer.newBarrier(), new IgnoreExceptionHandler(), consumers);
        //每个消费者，也就是 workProcessor都有一个sequence，表示上一个消费的位置,这个在初始化时都是-1
        Sequence[] sequences = workerPool.getWorkerSequences();
        //将其保存在ringBuffer中的 sequencer 中，在为生产申请slot时要用到,也就是在为生产者申请slot时不能大于此数组中的最小值,否则产生覆盖
        ringBuffer.addGatingSequences(sequences);
        //用executor 来启动 workProcessor 线程
        workerPool.start(consumerExecutor);

        //生产者开始生产数据
        for (int i = 0; i < processor; i++) {
            DisruptorProducer producer = new DisruptorProducer(ringBuffer);
            produceerExecutor.submit(producer);
        }
    }

}
