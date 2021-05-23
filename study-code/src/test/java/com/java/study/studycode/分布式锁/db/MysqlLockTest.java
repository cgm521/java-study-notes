package com.java.study.studycode.分布式锁.db;

import com.java.study.studycode.common.mysql.pool.ConnectionPool;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/5/23 下午4:56
 */

public class MysqlLockTest  {

    @Before
    public void init() {
        MysqlLock.init();
    }

    @After
    public void close() {
        ConnectionPool.close();
    }

    @SneakyThrows
    @Test
    public void test() {
        String lockKey = "test_lock";
        boolean test_lock = MysqlLock.lock(lockKey);
        System.out.println(Thread.currentThread().getName() + " lock :" + test_lock);
        new Thread(() -> {
            while (true) {
                boolean b = MysqlLock.lock(lockKey);
                System.out.println(Thread.currentThread().getName() + " lock :" + b);
                if (!b) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                MysqlLock.unLock(lockKey);
            }
        }).start();
        Thread.sleep(6000);
        boolean b = MysqlLock.unLock(lockKey);
    }

}
