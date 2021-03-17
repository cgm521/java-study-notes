package com.java.study.studycode.collection;

import org.junit.Test;

import java.util.*;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/3/16 下午10:24
 */

public class SetDemo {

    @Test
    public void TreeSetTest() {
        Set<Integer> set = new TreeSet<>();
        set.add(1);
        set.add(21);
        set.add(3);
        Iterator<Integer> iterator = set.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    @Test
    public void LinkedHashSetTest() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add("1");
        set.add("3");
        set.add("2");
        set.forEach(System.out::println);
    }
}
