package com.java.study.studycode.disruptor;

import com.java.study.studycode.disruptor.obj.LongObj;
import com.lmax.disruptor.EventHandler;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/3/14 下午6:10
 */

public class DisruptorEventSignalStartCustomer implements EventHandler<LongObj> {
    @Override
    public void onEvent(LongObj event, long sequence, boolean endOfBatch) throws Exception {
        event.setTName(Thread.currentThread().getName());
    }
}
