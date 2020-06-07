package com.java.study.studycode;

import com.alibaba.druid.pool.DruidDataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
@MapperScan("com.java.study.studycode.dao")
public class StudyCodeApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(StudyCodeApplication.class, args);
        System.out.println("启动成功");
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(StudyCodeApplication.class);
    }
//    @Bean
//    @ConfigurationProperties(prefix = "db")
//    public DataSource dateSource() {
//        DruidDataSource dataSource = new DruidDataSource();
//        return dataSource;
//    }
}
