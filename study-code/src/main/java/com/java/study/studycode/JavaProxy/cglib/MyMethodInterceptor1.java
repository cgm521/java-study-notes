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
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author cgm
 * @version $Id: MyMethodInterceptor1.java, v 0.1 2018-06-04 14:46 cgm Exp $$
 * cglib第一种写法
 */
public class MyMethodInterceptor1 implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("---cglib1---事务开始"+methodProxy.getSuperName());
        Object invokeSuper = methodProxy.invokeSuper(o, objects);
//        methodProxy.invokeSuper(o, objects);
        System.out.println("---cglib1---事务结束"+methodProxy.getSuperName());
        return invokeSuper;
    }

    public static void main(String[] args) {

        MyMethodInterceptor1 myMethodInterceptor1 = new MyMethodInterceptor1();
        Enhancer enhancer = new Enhancer();
        // todo setSuperclass不可是接口，必须是实现类，否则会报错 java.lang.NoSuchMethodError
        enhancer.setSuperclass(UserServiceImpl.class);
        enhancer.setCallback(myMethodInterceptor1);
        UserServiceImpl o = (UserServiceImpl)enhancer.create();

        System.out.println(o.getName());
        System.out.println(o.getAge());
        PriorityBlockingQueue<String> strings = new PriorityBlockingQueue<>();
    }
}
