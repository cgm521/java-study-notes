package com.java.study.studycode.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import com.java.study.studycode.enums.PersonStatusEnum;
import com.java.study.studycode.mapper.PersonMapper;
import com.java.study.studycode.entry.Person;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/7/3 下午11:16
 */

public class MybatisDemo {

    public static void main(String[] args) throws Exception {

        DataSource dataSource =getDataSource();
        ServiceLoader<MybatisDemo> load = ServiceLoader.load(MybatisDemo.class);
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(PersonMapper.class);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
//        Person person = new Person();
//        person.setId(10L);
//        person.setAge(1);
//        person.setName("aaa");
//        person.setStatus(PersonStatusEnum.VALID);
//        personMapper.insert(person);
//        List<Person> people = personMapper.selectAll();
//        System.out.println(people);
        Boolean check = personMapper.check();
        System.out.println(check);
//
//        Connection connection = DriverManager.getConnection("");
//        Statement statement = connection.createStatement();
//        boolean execute = statement.execute("");
//        ResultSet resultSet = statement.getResultSet();
//        ResultSetMetaData metaData = resultSet.getMetaData();
//        int columnCount = metaData.getColumnCount();
//        resultSet.next();
//        resultSet.getString(1);


    }

    private static DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/test");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        return dataSource;
    }
}
