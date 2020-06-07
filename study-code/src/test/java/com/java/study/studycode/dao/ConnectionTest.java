package com.java.study.studycode.dao;

import com.java.study.studycode.BaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2020/6/6 7:29 下午
 */

public class ConnectionTest extends BaseTest {
    @Resource
    private ConnectionDao connectionDao;

    @Test
    public void connectionTest1() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch countDownLatch = new CountDownLatch(3);
        Runnable runnable = () -> {
            connectionDao.insert();
            connectionDao.select();
            connectionDao.update();
            connectionDao.delete();
            countDownLatch.countDown();
        };
        for (int i = 0; i < 3; i++) {
            executorService.submit(runnable);
        }
        countDownLatch.await();
        executorService.shutdown();
    }
}
