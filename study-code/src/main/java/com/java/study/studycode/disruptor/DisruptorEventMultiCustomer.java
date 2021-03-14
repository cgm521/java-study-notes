package com.java.study.studycode.disruptor;

import com.example.dailypractice.disruptor.obj.LongObj;
import com.lmax.disruptor.WorkHandler;

/**
 * @Author:wb-cgm503374
 * @Description 事件处理器 消费者
 * @Date:Created in 2021/3/13 下午9:49
 */

public class DisruptorEventMultiCustomer implements WorkHandler<LongObj> {
    private int count = 0;

    public DisruptorEventMultiCustomer() {
    }

    @Override
    public void onEvent(LongObj event) throws Exception {
        ;count++;
        System.out.println("multi customer event " + Thread.currentThread().getName() + "  " + event);
    }

    public int getCount() {
        return count;
    }
}
