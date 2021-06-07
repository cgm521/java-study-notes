package com.java.study.studycode.MySingleton;

/**
 * @auth cgm
 * @date 2017/12/9 19:43
 * 懒汉模式  添加同步机制 线程安全
 */
public class singleton_3 {
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> System.out.println(MyObject_3.getInstance().hashCode())).start();
        }
    }
}

class MyObject_3{
    private static MyObject_3 myObject;

    public MyObject_3() {
        System.out.println("gouzao");
    }

    synchronized public static MyObject_3 getInstance() {
        if (null == myObject) {
            myObject =new MyObject_3();
        }
        return myObject;
    }
}
