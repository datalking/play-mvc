package com.github.datalking.context;

/**
 * @author yaoo on 6/2/18
 */
public interface ApplicationEventPublisher {

    /**
     * 将事件通知所有监听器
     */
    void publishEvent(ApplicationEvent event);

}
