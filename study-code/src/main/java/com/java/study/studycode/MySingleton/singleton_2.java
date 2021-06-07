package com.java.study.studycode.MySingleton;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @auth cgm
 * @date 2017/12/9 19:38
 * 懒汉模式 非线程安全
 */
public class singleton_2 {
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> System.out.println(MyObject_2.getInstance().hashCode())).start();
        }
    }
}

class MyObject_2{
    private static MyObject_2 myObject;

    public MyObject_2() {
        System.out.println("gouzao");
    }

    public static MyObject_2 getInstance() {
        if (null == myObject) {
            myObject =new MyObject_2();
        }
        return myObject;
    }
}
