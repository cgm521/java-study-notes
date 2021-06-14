package com.java.study.studycode.leetcode;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/5/25 9:42 上午
 */

public class 爬楼梯 {

    private long compute3(int n) {
        int[] total = new int[n];
        total[0] = 1;
        for (int i = 1; i < n; i++) {
            if (i > 1) {
                total[i] = total[i - 1] + total[i - 2];
            } else {
                total[i] = total[i - 1] + 1;
            }
        }
        return total[n-1];
    }
    private long compute2(int step) {
        long current = 1;
        long pre = 1;
        long prepre = 1;
        for (int i = 1; i < step; i++) {
            current = pre + prepre;
            prepre = pre;
            pre = current;
        }
        return current;
    }

    private int compute(int step) {
        if (step <= 0) {
            return 0;
        }
        if (step == 1) {
            return 1;
        }
        if (step == 2) {
            return 2;
        }
        return compute(step - 1) + compute(step - 2);
    }
}
