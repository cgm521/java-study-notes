package com.java.study.studycode.MySingleton;

/**
 * @auth cgm
 * @date 2017/12/9 19:57
 * 静态内置类 线程安全
 */
public class singleton_6 {
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> System.out.println(MyObject_6.getInstance().hashCode())).start();
        }
    }
}

class MyObject_6{
    private static class MyObject_6_inner{
        private static MyObject_6 myObject=new MyObject_6();
    }

    public MyObject_6() {
        System.out.println("构造");
    }

    public static MyObject_6 getInstance() {
        return MyObject_6_inner.myObject;
    }
}