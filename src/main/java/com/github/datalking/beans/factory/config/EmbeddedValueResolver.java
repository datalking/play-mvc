package com.github.datalking.beans.factory.config;

import com.github.datalking.common.StringValueResolver;

/**
 * @author yaoo on 5/6/18
 */
public class EmbeddedValueResolver implements StringValueResolver {

    private final BeanExpressionContext exprContext;

    private final BeanExpressionResolver exprResolver;

    public EmbeddedValueResolver(ConfigurableBeanFactory beanFactory) {
        this.exprContext = new BeanExpressionContext(beanFactory, null);
        this.exprResolver = beanFactory.getBeanExpressionResolver();
    }


    @Override
    public String resolveStringValue(String strVal) {
        String value = this.exprContext.getBeanFactory().resolveEmbeddedValue(strVal);
        if (this.exprResolver != null && value != null) {
            Object evaluated = this.exprResolver.evaluate(value, this.exprContext);
            value = (evaluated != null ? evaluated.toString() : null);
        }
        return value;
    }

}

