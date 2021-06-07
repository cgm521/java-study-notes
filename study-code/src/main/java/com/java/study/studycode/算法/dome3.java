package com.java.study.studycode.算法;


import com.java.study.studycode.算法.sort.QuickSort;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author caiguangming
 * @version $Id: dome3.java, v 0.1 2018-12-15 下午10:26 caiguangming Exp $$
 * 阿里面试算法
 * 给定一个int数组,返回这个数组中和等于给定数值的两个元素的下标.例如:
 * 给定 nums = [2, 7, 11, 15], target = 9,
 * 因为 nums[0] + nums[1] = 2 + 7 = 9,return [0, 1].
 * 要求时间复杂度为O(n),即同一个元素只允许使用一次.
 */
public class dome3{
    public static void main(String[] args) {
        Integer[] nums = {2,11, 15, 7, 9} ;
        Integer target = 9;
        Integer[] method1 = method1(nums, target);
        Integer[] method2 = method3(nums, target);
        System.out.println(Arrays.toString(method2));
    }

    public static Integer[] method1(Integer[] source, Integer target) {
        Integer[] indexes = new Integer[2];
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source.length; j++) {
                if (source[i] + source[j] == target) {
                    indexes[0] = i;
                    indexes[1] = j;
                    return indexes;
                }
            }
        }
        return indexes;
    }

    /**
     * 利用hashMap缓存数据
     * @param source
     * @param target
     * @return
     */
    public static Integer[] method2(Integer[] source, Integer target) {
        Integer[] indexs = new Integer[2];
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < source.length; i++) {
            map.put(source[i], i);
            if (map.get(target - source[i]) != null) {
                indexs[0] = map.get(target - source[i]);
                indexs[1] = i;
                break;
            }

        }

        return indexs;
    }

    public static Integer[] method3(Integer[] source, Integer target) {
        Integer[] indexs = new Integer[2];
        QuickSort.sort(source, 0, source.length-1);
        System.out.println(Arrays.toString(source));
        for (int i = 0; i < source.length; i++) {
            int search = ErFenFind.binarySearch(source, target - source[i]);
            if (search > 0) {
                indexs[0] =i;
                indexs[1] = search;
                break;
            }

        }
        return indexs;
    }

}
