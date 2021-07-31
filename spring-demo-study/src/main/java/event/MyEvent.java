package event;

import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * @Author:wb-cgm503374
 * @Description
 * @Date:Created in 2021/7/19 下午11:22
 */
@ToString
public class MyEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MyEvent(Object source) {
        super(source);
    }
}
