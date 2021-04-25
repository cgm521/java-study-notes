package com.java.study.studycode.dome;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2020/6/14 8:49 下午
 */

public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            new Thread(new Work(startLatch, doneLatch)).start();
        }

        System.out.println(Thread.currentThread().getName() + "-" + "main方法 1111");
        startLatch.countDown();
        System.out.println(Thread.currentThread().getName() + "-" + "main方法 2222");
        doneLatch.await();
        System.out.println(Thread.currentThread().getName() + "-" + "main方法 3333");
    }

    @Test
    public  void test() {
        int i = Math.abs("test-consumer-group".hashCode()) % 50;
        System.out.println(i);
    }
    static class Work implements Runnable {
        CountDownLatch startLatch;
        CountDownLatch doneLatch;

        Work(CountDownLatch startLatch, CountDownLatch doneLatch) {
            this.startLatch = startLatch;
            this.doneLatch = doneLatch;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
                startLatch.await();
                System.out.println(Thread.currentThread().getName()+"-"+"doSomething 99999");
                doneLatch.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}
