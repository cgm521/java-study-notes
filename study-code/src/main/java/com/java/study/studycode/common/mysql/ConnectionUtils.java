package com.java.study.studycode.common.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2020/6/6 7:14 下午
 */


public class ConnectionUtils {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String USER = "root";
    private static final String PWD = "123456";
    private static final String URL = "jdbc:mysql://localhost:3306/test";

    //定义一个数据库连接
    private static Connection conn = null;
    private static ThreadLocal<Connection> connContainer = new ThreadLocal<Connection>();
    //获取连接
    public synchronized static Connection getConnection() {
        //获取连接对象
        conn = connContainer.get();
        try {
            if(conn == null) {
                Class.forName(DRIVER);
                conn = DriverManager.getConnection(URL, USER, PWD);
                connContainer.set(conn);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }
    //关闭连接
    public static void closeConnection() {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        System.out.println(getConnection());
    }
}