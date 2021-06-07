/**
 * Choicesoft.com.cn Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.java.study.studycode.算法.sort;

/**
 * @author cgm
 * @version $Id: MaoPaoSort.java, v 0.1 2018-08-05 16:14 cgm Exp $$
 *          O(N2)
 *          稳定
 */
public class MaoPaoSort {

    public static void sort(int[] ints) {
        int length = ints.length;
        boolean flag = true;
        int count = 0;
        for (int i = 0; i < length - 1; i++) {
            flag = true;
            for (int j = 0; j < length - i - 1; j++) {
                count++;
                if (ints[j] > ints[j + 1]) {
                    flag = false;
                    int tmp = ints[j];
                    ints[j] = ints[j + 1];
                    ints[j + 1] = tmp;
                }
            }
            if (flag) {
                break;
            }
        }
        System.out.println("count " + count);
    }
}
