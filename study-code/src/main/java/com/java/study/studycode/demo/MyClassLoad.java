package com.java.study.studycode.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/6/14 下午8:28
 */

public class MyClassLoad extends ClassLoader {
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = new byte[0];
        try {
            bytes = getBytes("/Users/caiguangming/my-work/java-study-notes/study-code/src/main/java/com/java/study/studycode/demo/Hello.xlass");
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte)(255 - bytes[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defineClass(name, bytes, 0, bytes.length);
    }

    private static byte[] getBytes(String fileName) throws IOException {
        File file = new File(fileName);
        int length = (int)file.length();
        byte[] bytes = new byte[length];
        new FileInputStream(file).read(bytes);
        return bytes;
    }
    public static void main(String[] args) {
        try {
            Class<?> aClass = new MyClassLoad().findClass("Hello");
            Object hello = aClass.newInstance();
            Method method = aClass.getMethod("hello");
            method.invoke(hello);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
