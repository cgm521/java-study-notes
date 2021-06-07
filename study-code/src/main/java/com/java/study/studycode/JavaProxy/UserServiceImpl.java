/**
 * Choicesoft.com.cn Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.java.study.studycode.JavaProxy;

/**
 * @author cgm
 * @version $Id: UserServiceImpl.java, v 0.1 2018-06-01 17:49 cgm Exp $$
 */
public class UserServiceImpl implements UserService {

    @Override
    public String getName() {
        System.out.println("UserServiceImpl getName");
        return "tom";
    }

    public String getAge() {
        System.out.println("getAge");
        return "12";
    }
    public static void sys() {
        System.out.println("static");
    }
}
