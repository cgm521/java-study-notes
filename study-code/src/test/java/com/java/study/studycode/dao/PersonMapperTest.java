package com.java.study.studycode.dao;

import com.google.common.collect.Lists;
import com.java.study.studycode.BaseTest;
import com.java.study.studycode.entry.Person;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/4/16 下午10:40
 */

public class PersonMapperTest extends BaseTest {
    @Resource
    private PersonMapper personMapper;
    @Test
    public void batchInsert() {
        Person person = new Person();
        person.setName("aaa");
        person.setAge(11);
        ArrayList<Person> list = Lists.newArrayList(person);
        int i = personMapper.batchInsert(list);
        System.out.println(list);
    }

    @Test
    public void selectTest() {
        Person person = personMapper.selectByPrimaryKey(1L);
        System.out.println(person);
    }


}
