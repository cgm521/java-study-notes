/**
 * Choicesoft.com.cn Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.java.study.studycode.JavaProxy.cglib;

import com.java.study.studycode.JavaProxy.UserServiceImpl;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author cgm
 * @version $Id: MyMethodInterceptor2.java, v 0.1 2018-06-04 14:54 cgm Exp $$
 *          cglib第二种写法
 */
public class MyMethodInterceptor2 implements MethodInterceptor {
    //目标对象
    private Object target;

    public MyMethodInterceptor2(Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("---cglib2---事务开始" + methodProxy.getSuperName());
        Object invoke = methodProxy.invoke(target, objects);
//        Object invoke = methodProxy.invokeSuper(o, objects);
        System.out.println("---cglib2---事务结束" + methodProxy.getSuperName());
        return invoke;
    }

    public static void main(String[] args) {
        // 构造函数传目标对象
        MyMethodInterceptor2 myMethodInterceptor2 = new MyMethodInterceptor2(new UserServiceImpl());
        Enhancer enhancer = new Enhancer();
        //todo setSuperclass 可以接口  也可以对象
        enhancer.setSuperclass(UserServiceImpl.class);
        enhancer.setCallback(myMethodInterceptor2);
        UserServiceImpl o = (UserServiceImpl) enhancer.create();

        System.out.println(o.getName());
    }
}
