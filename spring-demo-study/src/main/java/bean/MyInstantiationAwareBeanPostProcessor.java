package bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.beans.PropertyDescriptor;


/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/6/11 2:40 下午
 */

public class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        System.out.println(">>>《1、实例化前置处理》>>>MyInstantiationAwareBeanPostProcessor##BeforeInstantiation>>>" + beanName);
        return null;
    }
// 2 实例化
    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        System.out.println(">>>《3、实例化后置处理》>>>MyInstantiationAwareBeanPostProcessor##AfterInstantiation>>>" + beanName);
        return true;
    }

    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        System.out.println(">>>《属性设置》");
        return pvs;
    }
}
