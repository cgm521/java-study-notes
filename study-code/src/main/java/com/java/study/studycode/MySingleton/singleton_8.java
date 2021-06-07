package com.java.study.studycode.MySingleton;

/**
 * @auth cgm
 * @date 2017/12/9 22:00
 */
public class singleton_8 {
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> System.out.println(MyObject_8.conn.getInstance().hashCode())).start();
        }
    }
}

enum MyObject_8{
    conn;
    private  MyObject_7 myObject;

    private MyObject_8(){

        System.out.println("8 构造");
        if (null == myObject) {
            myObject=new MyObject_7();
        }
    }
    public MyObject_7 getInstance() {
        return myObject;
    }
}
