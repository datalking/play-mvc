package com.github.datalking.aop.support;

import com.github.datalking.aop.Pointcut;

/**
 * @author yaoo on 4/19/18
 */
public interface ExpressionPointcut extends Pointcut {

    /**
     * 获取切入点表达式
     */
    String getExpression();

}
