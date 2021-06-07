/**
 * Choicesoft.com.cn Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.java.study.studycode.算法.sort;

/**
 * @author cgm
 * @version $Id: SelectSort.java, v 0.1 2018-08-05 17:22 cgm Exp $$
 *          首先在未排序的数列中找到最小(or最大)元素，然后将其存放到数列的起始位置；
 *          接着，再从剩余未排序的元素中继续寻找最小(or最大)元素，然后放到已排序序列的末尾。
 *          以此类推，直到所有元素均排序完毕。
 *          O(N2)
 *          稳定
 */
public class SelectSort {
    public static void sort(int[] ints) {

        for (int i = 0; i < ints.length; i++) {
            int min = i;
            for (int j = i + 1; j < ints.length; j++) {
                if (ints[j] < ints[min]) {
                    min = j;
                }
            }
            int tmp = ints[i];
            ints[i] = ints[min];
            ints[min] = tmp;
        }
    }
}
