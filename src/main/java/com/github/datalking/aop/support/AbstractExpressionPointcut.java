package com.github.datalking.aop.support;

import java.io.Serializable;

/**
 * @author yaoo on 4/19/18
 */
public abstract class AbstractExpressionPointcut implements ExpressionPointcut, Serializable {

    private String location;

    private String expression;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
        onSetExpression(expression);

    }

    protected void onSetExpression(String expression) throws IllegalArgumentException {
    }


}
