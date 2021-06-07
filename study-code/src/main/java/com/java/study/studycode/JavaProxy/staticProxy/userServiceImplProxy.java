package com.java.study.studycode.JavaProxy.staticProxy;


import com.java.study.studycode.JavaProxy.UserService;
import com.java.study.studycode.JavaProxy.UserServiceImpl;

/**
 * @author cgm
 * @version $Id: userServiceImplProxy.java, v 0.1 2018-06-04 14:28 cgm Exp $$
 */
public class userServiceImplProxy implements UserService {
    //目标对象  需代理者
    private UserServiceImpl userService;

    public userServiceImplProxy(UserServiceImpl UserService) {
        this.userService = UserService;
    }
    //静态代理类的实现.代码已经实现好了
    @Override
    public String getName() {
        System.out.println("---static proxy-------事务开始----");
        String name = userService.getName();
        System.out.println("---static proxy-------事务开始----");
        return name;
    }

    public static void main(String[] args) {
        userServiceImplProxy proxy = new userServiceImplProxy(new UserServiceImpl());
        System.out.println(proxy.getName());
    }
}
