package bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/6/11 9:47 上午
 */

public class ShopInfoImpl implements ShopInfo, InitializingBean, DisposableBean , ApplicationContextAware , BeanFactoryAware {
    private UserInfo userInfo;
    public ShopInfoImpl() {
        System.out.println(">>>《2、实例化》>>>B#实例化");
    }
    @Override
    public String getShopInfo(String id) {
        String userInfo = this.userInfo.getUser(id);
        return "shop" + userInfo;
    }

    @Override
    public String getShop(String id) {
        return "shop";
    }

    public UserInfo getUserInfo() {
        System.out.println(">>>>>>>B#getUserInfo");
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        System.out.println(">>>>《4、填充属性》>>>B#setUserInfo");
        this.userInfo = userInfo;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(">>>《8、初始化》>>>>>>>B#InitializingBean#afterPropertiesSet");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println(">>>>>《10、销毁》>>>>>B#DisposableBean#destroy");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println(">>>《6、执行application Aware》>>>>>>>B#ApplicationContextAware#setApplicationContext");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println(">>>《5、执行bean Aware》>>>>>>>B#BeanFactoryAware#setBeanFactory");
    }

    public void init() {
        System.out.println(">>《9、init-method》>>>PostConstruct#init");
    }
}
