package com.github.datalking.aop.aspectj;

import com.github.datalking.aop.PointcutAdvisor;

/**
 * @author yaoo on 4/19/18
 */
public interface InstantiationModelAwarePointcutAdvisor extends PointcutAdvisor {

    // 这个advisor是否延迟初始化包含的advice
    boolean isLazy();

    // 这个advisor是否已经实例化advice
    boolean isAdviceInstantiated();

}
