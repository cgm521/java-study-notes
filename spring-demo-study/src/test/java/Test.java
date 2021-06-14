import bean.UserInfo;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/6/11 9:51 上午
 */

public class Test {

    @org.junit.Test
    public void demo1() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("/applicationContext.xml");
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        UserInfo userInfo = (UserInfo) applicationContext.getBean("A");
        System.out.println(userInfo.getUserInfo("1"));

        applicationContext.getBeanFactory().destroySingletons();
        System.out.println("销毁");
    }
}
