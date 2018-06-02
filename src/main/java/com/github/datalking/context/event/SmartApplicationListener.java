package com.github.datalking.context.event;

import com.github.datalking.common.Ordered;
import com.github.datalking.context.ApplicationEvent;
import com.github.datalking.context.ApplicationListener;

/**
 * @author yaoo on 6/2/18
 */
public interface SmartApplicationListener extends ApplicationListener<ApplicationEvent>, Ordered {

    boolean supportsEventType(Class<? extends ApplicationEvent> eventType);

    boolean supportsSourceType(Class<?> sourceType);

}
