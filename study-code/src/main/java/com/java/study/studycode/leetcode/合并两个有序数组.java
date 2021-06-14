package com.java.study.studycode.leetcode;

import java.util.Arrays;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/5/25 9:42 上午
 */

public class 合并两个有序数组 {

    public static void main(String[] args) {
        int[] intsn = {1, 5, 6};
        int[] intsm = {2, 6, 9};
//        System.out.println(Arrays.toString(method1(intsn, intsm)));

        Boolean a = null;
        System.out.println(Boolean.TRUE.equals(a));
    }

    private static int[] method1(int[] arrn,int[] arrm) {
        int n = arrn.length;
        int m = arrm.length;
        int[] ints = new int[n + m];
        int idx = 0;
        for (int in = 0, im = 0; in < n || im < m; ) {
            if (in < n && im < m) {
                // n和m都还有
                ints[idx++] = arrn[in] > arrm[im] ? arrm[im++] : arrn[in++];
            } else if (in < n) {
                // m 完了
                ints[idx++] = arrn[in++];
            }else {
                ints[idx++] = arrm[im++];
            }
        }
        return ints;
    }
}
