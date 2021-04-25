package com.java.study.studycode.enums;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/4/26 上午12:11
 */

public enum PersonStatusEnum implements DbEnum {
    VALID("1","有效"),
    UNVALID("0","无效"),
    ;
    String value;
    String name;

    PersonStatusEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
