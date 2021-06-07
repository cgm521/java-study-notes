/**
 * Choicesoft.com.cn Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.java.study.studycode.算法;

/**
 * @author cgm
 * @version $Id: ErFenFind.java, v 0.1 2018-06-13 16:30 cgm Exp $$
 *          二分查找法
 */
public class ErFenFind {

    public static int binarySearch(Integer[] ts, int t) {
        int left = 0, right = ts.length-1;

        int count = 0;
        while (left<=right) {
            int mid = (left + right) / 2;
            int midValue = ts[mid];
            System.out.println(mid);
            count++;
            if (midValue > t) {
                //前面
                right = mid - 1;
            } else if (midValue < t) {
                //后面
                left = mid + 1;
            } else {
                System.out.println("count=" + count);
                return mid;
            }
        }
        return -left;
    }

    public static void main(String[] args) {
        Integer[] ts = {0, 1,  3, 4, 5, 6, 7, 8, 9};
        System.out.println(binarySearch(ts, 2));
    }
}
