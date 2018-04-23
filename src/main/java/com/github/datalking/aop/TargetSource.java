package com.github.datalking.aop;

/**
 * 获取aop invocation的target
 *
 * @author yaoo on 4/18/18
 */
public interface TargetSource {

    Class<?> getTargetClass();

    Object getTarget();

}
