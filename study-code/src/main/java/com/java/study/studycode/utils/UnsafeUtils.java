package com.java.study.studycode.utils;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/7/31 下午6:48
 */

public class UnsafeUtils {

    public static UNSAFE getInstance() {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return (Unsafe) theUnsafe.get(null);
    }

}
