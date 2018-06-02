package com.github.datalking.context;

import java.util.EventObject;

/**
 * @author yaoo on 6/2/18
 */
public abstract class ApplicationEvent extends EventObject {

    private static final long serialVersionUID = 7099057708183571937L;

    /**
     * 事件发生的时间，单位为毫秒
     * System time when the event happened
     */
    private final long timestamp;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public ApplicationEvent(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }

    public final long getTimestamp() {
        return this.timestamp;
    }

}
