package com.java.study.studycode.MySingleton;

/**
 * @auth cgm
 * @date 2017/12/9 19:45
 * 懒汉模式  添加同步代码块 线程安全
 */
public class singleton_4 {
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> System.out.println(MyObject_4.getInstance().hashCode())).start();
        }
    }
}

class MyObject_4{
    private static MyObject_4 myObject;

    public MyObject_4() {
        System.out.println("gouzao");
    }

    public static MyObject_4 getInstance() {
        synchronized (MyObject.class){
            if (null == myObject) {
                myObject =new MyObject_4();
            }
        }

        return myObject;
    }
}
