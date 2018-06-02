package com.github.datalking.context.event;

import com.github.datalking.context.ApplicationEvent;
import com.github.datalking.context.ApplicationListener;

/**
 * @author yaoo on 6/2/18
 */
public interface ApplicationEventMulticaster {

    void addApplicationListener(ApplicationListener listener);

    void addApplicationListenerBean(String listenerBeanName);

    void removeApplicationListener(ApplicationListener listener);

    void removeApplicationListenerBean(String listenerBeanName);

    void removeAllListeners();

    void multicastEvent(ApplicationEvent event);

}
