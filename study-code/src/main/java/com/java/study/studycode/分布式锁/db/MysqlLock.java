package com.java.study.studycode.分布式锁.db;

import com.java.study.studycode.common.mysql.pool.ConnectionPool;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * @Author:wb-cgm503374
 * @Description 数据库实现分布式锁
 * @Date:Created in 2021/5/23 下午4:33
 */
@Slf4j
public class MysqlLock {
    private final static String CREATE_TABLE_SQL = "CREATE TABLE If Not Exists `test`.`lock_table`  (\n" +
            "  `id` bigint(0) NOT NULL AUTO_INCREMENT,\n" +
            "  `lock_name` varchar(255) NOT NULL COMMENT '锁名称',\n" +
            "  `owner` varchar(255) NOT NULL COMMENT '锁拥有者',\n" +
            "  `create_time` datetime(0) NOT NULL DEFAULT now() ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',\n" +
            "  `expiry_time` datetime(0) NULL COMMENT '到期时间',\n" +
            "  `desc` varchar(1024) NULL COMMENT '描述',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE INDEX `idx_lock_name`(`lock_name`)\n" +
            ");";

    @SneakyThrows
    public static void init() {
        ConnectionPool.init();
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement ps = connection.prepareStatement(CREATE_TABLE_SQL);
        ps.execute();
        connection.close();
    }

    @SneakyThrows
    public static boolean lock(String lockKey) {
        String owner = Thread.currentThread().getId() + "";
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement ps = null, insPs = null;
        try {
             ps = connection.prepareStatement("select * from `test`.`lock_table` where lock_name = ?");
            ps.setString(1, lockKey);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                String exitOwner = resultSet.getString("owner");
                return owner.equals(exitOwner);
            } else {
                // 不存在，加锁
                insPs = connection.prepareStatement("insert into `test`.`lock_table`(lock_name,owner,create_time) value (?,?,?)");
                insPs.setString(1, lockKey);
                insPs.setString(2, owner);
                insPs.setDate(3, new Date(System.currentTimeMillis()));
                int i = insPs.executeUpdate();
                if (i == 1) {
                    return true;
                }
                return false;
            }
        } finally {
            connection.close();
            if (ps != null) {
                ps.close();
            }
            if (null != insPs) {
                insPs.close();
            }
        }
    }

    @SneakyThrows
    public static boolean unLock(String lockKey) {
        String owner = Thread.currentThread().getId() + "";
        Connection connection = ConnectionPool.getConnection();
        PreparedStatement ps = null, updatePs = null;
        try {
            ps = connection.prepareStatement("select * from `test`.`lock_table` where lock_name = ?");
            ps.setString(1, lockKey);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                if (owner.equals(resultSet.getString("owner"))) {
                    updatePs = connection.prepareStatement("delete from `test`.`lock_table` where lock_name=?");
                    updatePs.setString(1, lockKey);
                    int i = updatePs.executeUpdate();
                    return i == 1;
                }
                log.info("unlock fail , lock 不属于当前线程");
                return false;
            }
            return false;
        } finally {
            connection.close();
            if (ps != null) {
                ps.close();
            }
            if (null != updatePs) {
                updatePs.close();
            }
        }
    }
}
