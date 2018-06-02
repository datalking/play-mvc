package com.github.datalking.context.event;

import com.github.datalking.aop.support.AopUtils;
import com.github.datalking.common.GenericTypeResolver;
import com.github.datalking.common.Ordered;
import com.github.datalking.context.ApplicationEvent;
import com.github.datalking.context.ApplicationListener;
import com.github.datalking.util.Assert;

/**
 * @author yaoo on 6/2/18
 */
public class GenericApplicationListenerAdapter implements SmartApplicationListener {

    private final ApplicationListener delegate;

    public GenericApplicationListenerAdapter(ApplicationListener delegate) {
        Assert.notNull(delegate, "Delegate listener must not be null");
        this.delegate = delegate;
    }


    public void onApplicationEvent(ApplicationEvent event) {
        this.delegate.onApplicationEvent(event);
    }

    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(this.delegate.getClass(), ApplicationListener.class);
        if (typeArg == null || typeArg.equals(ApplicationEvent.class)) {
            Class<?> targetClass = AopUtils.getTargetClass(this.delegate);
            if (targetClass != this.delegate.getClass()) {
                typeArg = GenericTypeResolver.resolveTypeArgument(targetClass, ApplicationListener.class);
            }
        }
        return (typeArg == null || typeArg.isAssignableFrom(eventType));
    }

    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    public int getOrder() {
        return (this.delegate instanceof Ordered ? ((Ordered) this.delegate).getOrder() : Ordered.LOWEST_PRECEDENCE);
    }

}
