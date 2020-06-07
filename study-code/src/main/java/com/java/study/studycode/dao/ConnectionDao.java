package com.java.study.studycode.dao;

import com.java.study.studycode.common.mysql.ConnectionUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2020/6/6 7:27 下午
 */
@Component
public class ConnectionDao {
    public void insert() {
        //获取连接
        Connection conn = ConnectionUtils.getConnection();
        System.out.println("Dao.insert()-->" + Thread.currentThread().getName() + conn);
    }

    public void delete() {
        //获取连接
        Connection conn = ConnectionUtils.getConnection();
        System.out.println("Dao.delete()-->" + Thread.currentThread().getName() + conn);
    }

    public void update() {
        //获取连接
        Connection conn = ConnectionUtils.getConnection();
        System.out.println("Dao.update()-->" + Thread.currentThread().getName() + conn);
    }

    public void select() {
        //获取连接
        Connection conn = ConnectionUtils.getConnection();
        System.out.println("Dao.select()-->" + Thread.currentThread().getName() + conn);
    }
}
