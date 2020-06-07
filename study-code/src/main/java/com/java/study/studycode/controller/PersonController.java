package com.java.study.studycode.controller;

import com.java.study.studycode.entry.Person;
import com.java.study.studycode.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2020/6/6 11:11 下午
 */
@Slf4j
@RestController
@RequestMapping("/person")
public class PersonController {
    @Autowired
    private PersonService personService;

    @RequestMapping("/selectAll")
    public List<Person> selectAll() {
        log.info("selectAll");
        return personService.selectAll();
    }

    @RequestMapping("/")
    public String index() {
        return "index";
    }
}
