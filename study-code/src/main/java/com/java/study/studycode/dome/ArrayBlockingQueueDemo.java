package com.java.study.studycode.dome;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2020/9/8 12:02 上午
 */

public class ArrayBlockingQueueDemo {

    public static void main(String[] args) {
        ArrayBlockingQueueD arrayBlockingQueueD = new ArrayBlockingQueueD(5);
        for (int i = 0; i < 100; i++) {
            new Thread(arrayBlockingQueueD::get).start();
        }
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            new Thread(() -> arrayBlockingQueueD.put(finalI + "")).start();
        }
    }

}
 class ArrayBlockingQueueD{
    Lock lock;
    Condition condition;
    String[] queue;
    int count;

    int putIndex;
    int getIndex;

    public ArrayBlockingQueueD(int capacity) {
        lock = new ReentrantLock();
        condition = lock.newCondition();
        this.putIndex = 0;
        this.getIndex = 0;
        this.count =0;
        this.queue = new String[capacity];
    }


    public void put(String val) {
        try {
            lock.lockInterruptibly();

            while (count  >= queue.length) {
                System.out.println("put await");
                condition.await();
            }
            System.out.println(count);
            queue[count] = val;
            System.out.println("put" + " val: " + val+ " count:"+ count);
            count++;
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public String get() {
        String val = null;
        try {
            lock.lockInterruptibly();
            while (count <= 0) {
                System.out.println("get await");
                condition.await();
            }
            System.out.println("-- "+count);
            val = queue[--count];
            System.out.println("get" + " val:" + val + " count: " + count);
            queue[count] = null;
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return val;
    }
}
