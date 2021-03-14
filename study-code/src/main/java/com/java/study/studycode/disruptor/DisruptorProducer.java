package com.java.study.studycode.disruptor;

import com.java.study.studycode.disruptor.obj.LongObj;
import com.lmax.disruptor.RingBuffer;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author:wb-cgm503374
 * @Description 生产者
 * @Date:Created in 2021/3/14 下午2:11
 */

public class DisruptorProducer implements Runnable {
    private RingBuffer<LongObj> ringBuffer;

    private AtomicLong aLong = new AtomicLong();
    public static final int NUMBER = 1000000;

    public DisruptorProducer(RingBuffer<LongObj> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    @Override
    public void run() {
//        System.out.println("Producer run...");
        for (int i = 0; i < NUMBER; i++) {
//            System.out.println("Producer push " + i);

            push();
        }
//        System.out.println("Producer countDown");
    }

    private void push() {
        //1.可以把ringBuffer看做一个事件队列，那么next就是得到下面一个事件槽
        long sequence = ringBuffer.next();
        try {
            //2.用上面的索引取出一个空的事件用于填充（获取该序号对应的事件对象）
            LongObj product = ringBuffer.get(sequence);
            //3.获取要通过事件传递的业务数据
            product.setVal(aLong.incrementAndGet());
        } finally {
            //4.发布事件
            //注意，最后的 ringBuffer.publish 方法必须包含在 finally 中以确保必须得到调用；
            // 如果某个请求的 sequence 未被提交，将会堵塞后续的发布操作或者其它的 producer。
            ringBuffer.publish(sequence);
        }
    }
}
