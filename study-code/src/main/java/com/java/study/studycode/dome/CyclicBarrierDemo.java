package com.java.study.studycode.dome;


import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CyclicBarrier;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2020/6/22 11:22 下午
 */

public class CyclicBarrierDemo {
    public static void main(String[] args) throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(5, () -> {
            System.out.println("出发了。。");
        });
        cyclicBarrier.isBroken();
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new BarrierWork(cyclicBarrier));
            thread.setName("第" + i + "人");
            thread.start();
            threads.add(thread);
            System.out.println(cyclicBarrier.getNumberWaiting());
//            if (cyclicBarrier.getNumberWaiting()==3) {
//                thread.interrupt();
//            }
//            if (cyclicBarrier.getParties() == 1) {
//                cyclicBarrier.reset();
//            }
        }
        for (Thread thread : threads) {
            thread.join();
        }
//        System.out.println();
//        ConcurrentSkipListMap<String,String> map = new ConcurrentSkipListMap<String,String>();
//        map.put("", "");
    }
}

class BarrierWork implements Runnable {
    private CyclicBarrier cyclicBarrier;

    public BarrierWork(CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + "-" + "start await,count:" + cyclicBarrier.getNumberWaiting());
        try {

            System.out.println(Thread.currentThread().getName() + "上车了");
            int await = cyclicBarrier.await();
            System.out.println(Thread.currentThread().getName() + " : " + await);
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
//        System.out.println(Thread.currentThread().getName() + "-" + "通过栅栏");
    }
}