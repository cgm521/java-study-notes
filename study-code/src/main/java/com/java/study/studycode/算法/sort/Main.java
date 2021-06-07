/**
 * Choicesoft.com.cn Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.java.study.studycode.算法.sort;

import java.util.Arrays;

/**
 * @author cgm
 * @version $Id: Main.java, v 0.1 2018-08-05 16:18 cgm Exp $$
 *          它会遍历若干次要排序的数列，每次遍历时，它都会从前往后依次的比较相邻两个数的大小；
 *          如果前者比后者大，则交换它们的位置。这样，一次遍历之后，最大的元素就在数列的末尾！
 *          采用相同的方法再次遍历时，第二大的元素就被排列在最大元素之前。重复此操作，直到整个数列都有序为止！
 */
public class Main {

    public static void main(String[] args) {
        int i = 0;
        System.out.println(i);
        int[] ints = {12, 23, 4, 8, 6, 0, 3};
        int[] ints2 = {1, 2, 4, 5, 6, 10, 13};

//        MaoPaoSort.sort(ints);

        System.out.println("----------------");
//        QuickSort.sort(ints, 0, ints.length-1);
//        SelectSort.sort(ints);

        InsertSort.sort(ints);
        System.out.println(Arrays.toString(ints));
    }
}
