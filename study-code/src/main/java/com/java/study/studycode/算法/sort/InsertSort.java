/**
 * Choicesoft.com.cn Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.java.study.studycode.算法.sort;

/**
 * @author cgm
 * @version $Id: InsertSort.java, v 0.1 2018-08-05 17:32 cgm Exp $$
 *          拿到一个数和前面已有的数进行对比，比前面的数小就放到前面
 */
public class InsertSort {
    public static void sort(int[] ints) {
        for (int i = 1; i < ints.length; i++) {
            for (int j = i; j > 0; j--) {
                if (ints[j] < ints[j - 1]) {
                    int temp = ints[j - 1];
                    ints[j - 1] = ints[j];
                    ints[j] = temp;
                }
//                if (j != i - 1) {
//                    int tmp = ints[i];
//                    int k;
//                    for (k = i - 1; k > j; k--) {
//                        ints[k + 1] = ints[k];
//                    }
//                    ints[j + 1] = tmp;
//                }

            }
        }
    }
}
