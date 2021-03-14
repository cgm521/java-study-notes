package com.java.study.studycode.disruptor;

import com.example.dailypractice.disruptor.obj.LongObj;
import com.lmax.disruptor.EventHandler;

import java.util.concurrent.CountDownLatch;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/3/14 下午2:47
 */

public class DisruptorEventSignalCustomer implements EventHandler<LongObj> {
    private int count = 0;
    private CountDownLatch latch;

    public DisruptorEventSignalCustomer(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onEvent(LongObj event, long l, boolean b) throws Exception {
        count++;
        System.out.println("signal customer event " + event);
        if (count == DisruptorProducer.NUMBER) {
            latch.countDown();
        }
    }

    public int getCount() {
        return count;
    }
}
