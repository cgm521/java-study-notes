package com.java.study.studycode.MySingleton;

/**
 * @auth cgm
 * @date 2017/12/9 19:54
 * 懒汉模式  DCL 线程安全
 */
public class singleton_5 {
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> System.out.println(MyObject_5.getInstance().hashCode())).start();
        }
    }
}

class MyObject_5{
    private static MyObject_5 myObject;

    public MyObject_5() {
        System.out.println("gouzao");
    }

    public static MyObject_5 getInstance() {
        if (null != myObject) {
        }else {
            synchronized (MyObject.class){
                if (null == myObject) {
                    myObject =new MyObject_5();
                }
            }
        }
        return myObject;
    }
}