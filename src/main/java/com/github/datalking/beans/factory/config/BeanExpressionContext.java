package com.github.datalking.beans.factory.config;

import com.github.datalking.util.Assert;

/**
 * 计算一个BeanDefinition内部的表达式的容器对象
 *
 * @author yaoo on 4/29/18
 */
public class BeanExpressionContext {

    private final ConfigurableBeanFactory beanFactory;

    private final Scope scope;

    public BeanExpressionContext(ConfigurableBeanFactory beanFactory, Scope scope) {
        Assert.notNull(beanFactory, "BeanFactory must not be null");
        this.beanFactory = beanFactory;
        this.scope = scope;
    }

    public final ConfigurableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    public final Scope getScope() {
        return this.scope;
    }

    public boolean containsObject(String key) {
        return (this.beanFactory.containsBean(key) ||
                (this.scope != null && this.scope.resolveContextualObject(key) != null));
    }

    public Object getObject(String key) {
        if (this.beanFactory.containsBean(key)) {
            return this.beanFactory.getBean(key);
        } else if (this.scope != null) {
            return this.scope.resolveContextualObject(key);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BeanExpressionContext)) {
            return false;
        }
        BeanExpressionContext otherContext = (BeanExpressionContext) other;
        return (this.beanFactory == otherContext.beanFactory && this.scope == otherContext.scope);
    }

    @Override
    public int hashCode() {
        return this.beanFactory.hashCode();
    }

}

