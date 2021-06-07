/**
 * Choicesoft.com.cn Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.java.study.studycode.算法.sort;

/**
 * @author cgm
 * @version $Id: QuickSort.java, v 0.1 2018-08-05 16:29 cgm Exp $$
 *          从数列中挑出一个基准值。
 *          将所有比基准值小的摆放在基准前面，所有比基准值大的摆在基准的后面(相同的数可以到任一边)；在这个分区退出之后，该基准就处于数列的中间位置。
 *          递归地把"基准值前面的子数列"和"基准值后面的子数列"进行排序。
 *          O(N*lgN) - O(N2)
 *          不稳定
 */
public class QuickSort {
    public static void sort(Integer[] ints, int l, int r) {
        if (l < r) {
            int i = l;
            int j = r;
            int x = ints[i];
            while (i < j) {
                //从右往左遍历 大于x的则往左移
                while (i < j && ints[j] > x) {
                    j--;
                }
                //ints[j] 不大于 x 则把该位置元素移动到x所在的位置 i 然后i+1
                if (i < j) {
                    ints[i++] = ints[j];
                }
                //往右遍历，元素小于x则往右移
                while (i < j && ints[i] < x) {
                    i++;
                }
                //找到元素ints[i] 不小于x 则把该位置元素移动到上面找到的小于x的位置 j
                if (i < j) {
                    ints[j--] = ints[i];
                }
            }
            //把第i个元素赋值位x
            ints[i] = x;
            //x左边为全部小于x，右边为全部大于x，再递归
            sort(ints, l, i - 1);
            sort(ints, i + 1, r);
        }

    }
}
