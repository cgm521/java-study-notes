package com.java.study.studycode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BaseTest {
    AtomicLong aLong = new AtomicLong(0);
    @Test
    public void test() {
        ScheduledExecutorService ex = Executors.newScheduledThreadPool(1);
        ex.scheduleAtFixedRate(() -> System.out.println(aLong.get()), 5, 5, TimeUnit.SECONDS);
        System.out.println(compute(100));
    }

    private int compute(int step) {
        aLong.getAndIncrement();
        if (step <= 0) {
            return 0;
        }
        if (step == 1) {
            return 1;
        }
        if (step == 2) {
            return 2;
        }
        return compute(step - 1) + compute(step - 2);
    }
}
