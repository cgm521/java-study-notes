package com.java.study.studycode.MySingleton;

/**
 * @auth cgm
 * @date 2017/12/9 20:02
 * 静态代码块 线程安全
 */
public class singleton_7 {
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> System.out.println(MyObject_7.getInstance().hashCode())).start();
        }
    }
}

class MyObject_7{
    private static MyObject_7 myObject;

    public MyObject_7() {
        System.out.println("gou");
    }
    static {
        System.out.println("static");
        myObject = new MyObject_7();
    }

    public static MyObject_7 getInstance() {
        return myObject;
    }
}
