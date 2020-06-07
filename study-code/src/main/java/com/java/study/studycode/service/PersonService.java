package com.java.study.studycode.service;

import com.java.study.studycode.dao.PersonMapper;
import com.java.study.studycode.entry.Person;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2020/6/6 11:12 下午
 */

@Service
public class PersonService {
    @Resource
    private PersonMapper personMapper;

    public List<Person> selectAll(){
        List<Person> list = personMapper.selectAll();
        return list;
    }
}
