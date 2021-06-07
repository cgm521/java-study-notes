/**
 * Choicesoft.com.cn Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.java.study.studycode.JavaProxy.jdkProxy;

import com.java.study.studycode.JavaProxy.UserService;
import com.java.study.studycode.JavaProxy.UserServiceImpl;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;

import java.lang.reflect.Method;


/**
 * @author cgm
 * @version $Id: myInvocationHandler.java, v 0.1 2018-06-04 14:34 cgm Exp $$
 */
public class myInvocationHandler implements InvocationHandler {
    //目标对象
    private Object target;

    public myInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("---jdk proxy---开始事务---"+method.getName());
        Object invoke = method.invoke(target, args);
        System.out.println("---jdk proxy---结束事务---"+method.getName());
        return invoke;
    }

    public static void main(String[] args) {
        //目标对象 需代理者
        UserServiceImpl service = new UserServiceImpl();
        myInvocationHandler handler = new myInvocationHandler(service);

        UserService userService = (UserService) Proxy.newProxyInstance(
                //目标对象的类加载器
                service.getClass().getClassLoader(),
                //目标对象实现的接口  目标对象必须有实现接口，没有实现接口不可用jdk代理
                service.getClass().getInterfaces(),
                //自定义的代理实现类
                handler);
        System.out.println(userService.getName());
    }
}
