package event;

import org.springframework.context.support.StaticApplicationContext;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/7/19 下午11:25
 */

public class Main {
    public static void main(String[] args) {
        StaticApplicationContext context = new StaticApplicationContext();
        context.addApplicationListener(new MyListener());
        context.refresh();

        MyEvent event = new MyEvent("context");
        context.publishEvent(event);
    }
}
