package com.github.datalking.aop;

import org.aopalliance.aop.Advice;

/**
 * Advisor持有Advice
 * <p>
 * 用于组织target、advice、joinpoint
 *
 * @author yaoo on 4/18/18
 */
public interface Advisor {

    Advice getAdvice();

}
