package com.java.study.studycode.disruptor.obj;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/3/13 下午10:19
 */
@Data
@ToString
public class LongObj extends DisObj {
    Long val;
    String tName;

    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        ArrayList<String> clone = (ArrayList<String>) list.clone();
        System.out.println(clone);
        clone.add(1, "aa");
        System.out.println(clone);
        System.out.println(list);
    }
}
