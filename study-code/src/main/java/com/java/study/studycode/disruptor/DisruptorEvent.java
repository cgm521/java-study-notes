package com.java.study.studycode.disruptor;

/**
 * @Author:wb-cgm503374
 * @Description 事件，放入ringBuffer的数据（事件）
 * @Date:Created in 2021/3/13 下午9:24
 */

public class DisruptorEvent {
    private Long event;

    public void setEvent(Long event) {
        System.out.println("setEvent...");
        this.event = event;
    }

    public Long getEvent() {
        System.out.println("getEvent...");
        return event;
    }
}
