package com.java.study.studycode.unsafedemo;

import com.java.study.studycode.entry.Person;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/7/12 下午10:51
 */

public class UnsafeDemo {
    public static void main(String[] args) throws Exception{
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        // 不走构造器，不执行init方法
        Person person = (Person) unsafe.allocateInstance(Person.class);
        System.out.println(person);
    }
}
