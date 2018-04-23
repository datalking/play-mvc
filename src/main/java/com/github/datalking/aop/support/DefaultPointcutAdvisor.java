package com.github.datalking.aop.support;

import com.github.datalking.aop.Pointcut;
import org.aopalliance.aop.Advice;

/**
 * 保存Advice和Pointcut 默认实现类
 * <p>
 * 可以使用任意类型的Pointcut和Advice，但是不能使用Introduction
 *
 * @author yaoo on 4/18/18
 */
public class DefaultPointcutAdvisor extends AbstractPointcutAdvisor {

    private Pointcut pointcut = Pointcut.TRUE;


    public DefaultPointcutAdvisor() {
    }

    public DefaultPointcutAdvisor(Advice advice) {
        this(Pointcut.TRUE, advice);
    }

    public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
        this.pointcut = pointcut;
        setAdvice(advice);
    }


    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
    }


    @Override
    public String toString() {
        return "DefaultPointcutAdvisor{" +
                "pointcut=" + pointcut +
                ", advice=" + getAdvice().toString() +
                "} ";
    }

}
