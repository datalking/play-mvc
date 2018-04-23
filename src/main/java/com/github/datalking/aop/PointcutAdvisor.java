package com.github.datalking.aop;

/**
 * 持有pointcut和advice
 *
 * @author yaoo on 4/18/18
 */
public interface PointcutAdvisor extends Advisor {

    Pointcut getPointcut();

}
