package com.github.datalking.aop.framework.adapter;

import com.github.datalking.aop.Advisor;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * @author yaoo on 4/19/18
 */
public interface AdvisorAdapter {

    boolean supportsAdvice(Advice advice);

    // 创建方法拦截器
    MethodInterceptor getInterceptor(Advisor advisor);

}
