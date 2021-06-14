package bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/6/11 4:51 下午
 */

public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(">>>《7、初始化前置处理》>>>MyBeanPostProcessor##BeforeInitialization>>>" + beanName);
        return null;
    }
// 8、初始化
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println(">>>《9、初始化后置处理》>>>MyBeanPostProcessor##AfterInitialization>>>" + beanName);
        return null;
    }
}
