package com.java.study.studycode.伪共享;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/7/31 下午6:38
 */

public class NoPaddingDemo {

    static final class NoPaddingLong {
        volatile long val;

        private static final sun.misc.Unsafe UNSAFE;
        private static final long valOffset;

        static {
            UNSAFE = UnsafeUtils.getInstance();
            NoPaddingDemo.NoPaddingLong.class
        }
    }
}
