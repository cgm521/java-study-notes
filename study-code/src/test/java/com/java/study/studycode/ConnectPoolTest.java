package com.java.study.studycode;

import com.java.study.studycode.common.mysql.pool.ConnectionPool;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/5/22 下午11:38
 */

public class ConnectPoolTest {

    @Test
    public void test1() throws SQLException, ClassNotFoundException {
        ConnectionPool.init();

        Connection connection = ConnectionPool.getConnection();

        PreparedStatement ps = connection.prepareStatement("select 1");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String string = rs.getString(1);
            System.out.println(string);
        }
        connection.close();
    }
}
