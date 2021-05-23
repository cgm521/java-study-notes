package com.java.study.studycode.common.mysql.pool;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/5/22 下午11:10
 */
@Slf4j
public class ConnectionProxy implements InvocationHandler {

    private Connection target;

    public ConnectionProxy(Connection target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("connection proxy start.....");
        if (!"close".equals(method.getName())) {
            return method.invoke(target, args);
        }

        return true;
    }
}
