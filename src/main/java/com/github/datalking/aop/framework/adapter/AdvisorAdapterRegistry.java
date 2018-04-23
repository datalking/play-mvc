package com.github.datalking.aop.framework.adapter;

import com.github.datalking.aop.Advisor;
import org.aopalliance.intercept.MethodInterceptor;

/**
 * 注册advisor adapter的接口
 * <p>
 * 是SPI接口
 *
 * @author yaoo on 4/19/18
 */
public interface AdvisorAdapterRegistry {

    // 将advice包装成advisor
    Advisor wrap(Object advice) ;

    // 获取方法拦截器
    MethodInterceptor[] getInterceptors(Advisor advisor) ;

    // 注册AdvisorAdapter
    void registerAdvisorAdapter(AdvisorAdapter adapter);


}
