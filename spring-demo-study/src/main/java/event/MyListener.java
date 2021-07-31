package event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/7/19 下午11:23
 */

public class MyListener implements ApplicationListener<MyEvent> {

    @Override
    public void onApplicationEvent(MyEvent event) {
        System.out.println(">>>>>>>>>>MyListener");
        System.out.println(event.getSource());
    }
}
