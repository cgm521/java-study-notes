package com.java.study.studycode.java8;

import java.util.HashMap;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/3/21 下午6:19
 */

public interface IntinterfaceDemo {
    default void print() {
        System.out.println("default");
    }

    public static void main(String[] args) {
        IntinterfaceDemo intinterfaceDemo = new IntinterfaceDemo(){};
        intinterfaceDemo.print();

        new IntinterfaceDemo(){
            @Override
            public void print() {
                System.out.println("Override print");
            }
        }.print();

        HashMap<String, String> map = new HashMap<>();
        map.put("1", "1");
        System.out.println(map.merge("1", "a", String::concat));
        System.out.println(map.get("1"));


    }
}
