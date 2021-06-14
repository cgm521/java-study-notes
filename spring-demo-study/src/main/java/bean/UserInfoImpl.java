package bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/6/11 9:45 上午
 */

public class UserInfoImpl implements UserInfo, InitializingBean, DisposableBean , ApplicationContextAware, BeanFactoryAware {
    private ShopInfo shopInfo;

    public UserInfoImpl() {
        System.out.println(">>>《2、实例化》>>>A#实例化");
    }

    @Override
    public String getUserInfo(String id) {
        String shopInfo = this.shopInfo.getShop(id);
        return "user" + shopInfo;
    }

    @Override
    public String getUser(String id) {
        return "user";
    }

    public ShopInfo getShopInfo() {
        System.out.println(">>>>>>>A#getShopInfo");
        return shopInfo;
    }

    public void setShopInfo(ShopInfo shopInfo) {
        System.out.println(">>>《4、填充属性》>>>>A#setShopInfo");
        this.shopInfo = shopInfo;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(">>>《8、初始化》  >>>>>>>A#InitializingBean#afterPropertiesSet");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println(">>>《10、销毁》>>>>>>>A#DisposableBean#destroy");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println(">>>《6、执行application Aware》>>>>>>>A#ApplicationContextAware#setApplicationContext");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println(">>>《5、执行bean Aware》>>>>>>>A#BeanFactoryAware#setBeanFactory");
    }
}
