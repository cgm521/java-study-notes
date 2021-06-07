/**
 * Choicesoft.com.cn Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.java.study.studycode.算法;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cgm
 * @version $Id: MyMethodInterceptor1.java, v 0.1 2018-05-30 15:52 cgm Exp $$
 *          头条面试算法题：一副扑克牌A-K13张不知道顺序 第1张放在桌子上 第2张放在最后面 第3张放在桌子上 第4张放在最后面 以此类推 直到13张牌都放在桌子上 最后桌子上是A-K顺序 求原顺序
 */
public class dome1 {
    public static void main(String[] args) {
        long s = System.currentTimeMillis();
        int[] in = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < in.length; i++) {
            first(list, in[in.length - i - 1]);
            if (in.length - i - 1 > 0) {
                //最后一张牌不用放在最后
                end(list, in[in.length - i - 1]);
            }
        }
        System.out.println(list);
        System.out.println(System.currentTimeMillis()-s);
    }

    //元素放到数组第一位
    public static void first(List<Integer> ints, int a) {
        if (ints == null) {
            return;
        }
        List<Integer> temp = new ArrayList<>(ints);
        ints.clear();
        ints.add(a);
        ints.addAll(temp);
    }

    //最后一个放到第一个
    public static void end(List<Integer> ints, int a) {
        if (ints == null) {
            return;
        }
        List<Integer> temp = new ArrayList<>(ints);
        ints.clear();
        ints.add(temp.get(temp.size() - 1));
        temp.remove(temp.size() - 1);
        ints.addAll(temp);
    }


}
