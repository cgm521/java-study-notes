package bean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/6/14 上午10:33
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ComOrder implements InitializingBean , DisposableBean {
    private UserInfo userInfo;

    @PostConstruct
    public void init() {
        System.out.println(">>>ComOrder#PostConstruct");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(">>>《8、初始化》>>>>>>>ComOrder#InitializingBean#afterPropertiesSet");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println(">>>>ComOrder#destroy");
    }
}
