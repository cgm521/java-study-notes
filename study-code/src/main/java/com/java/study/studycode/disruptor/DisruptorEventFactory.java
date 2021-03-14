package com.java.study.studycode.disruptor;

import com.example.dailypractice.disruptor.obj.LongObj;
import com.lmax.disruptor.EventFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author:wb-cgm503374
 * @Description 事件工厂
 * Disruptor 通过 EventFactory 在 RingBuffer 中预创建 Event 的实例。
 * 一个 Event 实例实际上被用作一个“数据槽”，发布者发布前，先从 RingBuffer 获得一个 Event 的实例，然后往 Event 实例中填充数据，之后再发布到 RingBuffer 中，之后由 Consumer 获得该 Event 实例并从中读取数据
 * @Date:Created in 2021/3/13 下午9:29
 */

public class DisruptorEventFactory implements EventFactory<LongObj> {
    private final static AtomicInteger aCount = new AtomicInteger(0);
    @Override
    public LongObj newInstance() {
        aCount.getAndIncrement();
//        System.out.println("newInstance...");
        return new LongObj();
    }

    public static AtomicInteger getaCount() {
        return aCount;
    }
}
