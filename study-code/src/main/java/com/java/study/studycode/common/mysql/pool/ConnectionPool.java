package com.java.study.studycode.common.mysql.pool;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/5/22 下午11:15
 */
@Slf4j
public class ConnectionPool {
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String USER = "root";
    private static final String PWD = "123456";
    private static final String URL = "jdbc:mysql://localhost:3306/mysql";

    private static LinkedBlockingQueue<Connection> queue = new LinkedBlockingQueue<>();
    private static ThreadLocal<Connection> connContainer = new ThreadLocal<>();

    private final static int MIN_POOL_SIZE = 4;
    private final static int MAX_POOL_SIZE = 10;
    private static AtomicInteger LIVE_SIZE = new AtomicInteger(0);
    private static volatile boolean poolClose = false;

    public static void init() {
        for (int i = 0; i < MIN_POOL_SIZE; i++) {
            try {
                queue.put(newConnection());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("init success=" + LIVE_SIZE.get());
    }

    public static Connection getConnection() {
        Connection connection = connContainer.get();
        if (connection != null) {
            return connection;
        }
        connection = queue.poll();
        if (connection != null) {
            log.info("connection pool connection size:{}", queue.size());
            return connection;
        }
        if (LIVE_SIZE.get() < MAX_POOL_SIZE) {
            connection = newConnection();
        } else {
            throw new RuntimeException("连接获取失败！！");
        }


        return connection;
    }

    public static Connection getConnection(long timeout, TimeUnit unit) throws SQLException, ClassNotFoundException {
        Connection connection = connContainer.get();
        if (connection != null) {
            return connection;
        }
        connection = queue.poll();
        if (connection != null) {
            return connection;
        }
        if (LIVE_SIZE.get() < MAX_POOL_SIZE) {
            connection = newConnection();
        } else {
            try {
                connection = queue.poll(timeout, unit);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("连接获取失败！！");
            }
        }
        return connection;
    }

    public boolean isAvailable(Connection connection) {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    // 连接池关闭
    @SneakyThrows
    public static void close() {
        poolClose = true;
        log.info("pool close size:{}", queue.size());
        for (Connection connection = queue.poll(); connection != null; connection = queue.poll()) {
            connection.close();
        }
    }

    private static Connection newConnection() {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PWD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ConnectionProxy proxy = new ConnectionProxy(connection);
        connection = (Connection) Proxy.newProxyInstance(connection.getClass().getClassLoader(), connection.getClass().getInterfaces(), proxy);
        LIVE_SIZE.getAndIncrement();
        return connection;
    }

    static class ConnectionProxy implements InvocationHandler {

        private Connection target;
        private int time;

        public ConnectionProxy(Connection target) {
            this.target = target;
            time = 0;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (!"close".equals(method.getName()) || poolClose) {
                return method.invoke(target, args);
            }
            connContainer.remove();
            // 把代理类放入队列
            queue.put((Connection) Proxy.newProxyInstance(proxy.getClass().getClassLoader(), proxy.getClass().getInterfaces(), this));
            time++;
            log.info("连接关闭。。{}。代理。。。connection pool connection size:{}", time, queue.size());
            return true;
        }
    }
}
