package com.java.study.studycode.分布式锁.zookeeper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.Collections;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/5/23 下午10:30
 */
@Slf4j
public class ZookeeperLock {
    private static ZooKeeper zooKeeper;

    static {
        try {
            zooKeeper = new ZooKeeper("localhost:2181", 2000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static void lock(String path) {
        Object o = new Object();
        try {
            String s = zooKeeper.create(path, Thread.currentThread().getName().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE
                    , CreateMode.EPHEMERAL);
            log.info("lock success...");
        } catch (KeeperException | InterruptedException e) {
//            e.printStackTrace();
            zooKeeper.getChildren(path, new Watcher() {
                @SneakyThrows
                @Override
                public void process(WatchedEvent watchedEvent) {
                    log.info("watch start...");
                    synchronized (o) {
                        o.notifyAll();
                    }
                }
            });
            synchronized (o) {
                o.wait();
                lock(path);
            }
        }

    }

    @SneakyThrows
    public static void unlock(String path) {
        zooKeeper.delete(path, 0);
    }
}
