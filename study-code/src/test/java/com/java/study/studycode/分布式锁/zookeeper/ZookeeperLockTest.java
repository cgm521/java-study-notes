package com.java.study.studycode.分布式锁.zookeeper;

import com.java.study.studycode.BaseTest;
import lombok.SneakyThrows;
import org.junit.Test;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/5/23 下午10:39
 */

public class ZookeeperLockTest  {

    @SneakyThrows
    @Test
    public void test() {
        String path = "/lock";
        new Thread(()->{
            ZookeeperLock.lock(path);
            System.out.println("1 get lock");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ZookeeperLock.unlock(path);
        }).start();
        new Thread(()->{
            ZookeeperLock.lock(path);
            System.out.println("2 get lock");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ZookeeperLock.unlock(path);
        }).start();
        Thread.sleep(10000);
    }

}
