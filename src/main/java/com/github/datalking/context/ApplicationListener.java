package com.github.datalking.context;

import java.util.EventListener;

/**
 * @author yaoo on 6/2/18
 */
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

    /**
     * 监听到event事件后的处理
     */
    void onApplicationEvent(E event);

}
