package com.github.datalking.aop;

/**
 * @author yaoo on 4/18/18
 */
public interface ClassFilter {

    boolean matches(Class<?> clazz);

    ClassFilter TRUE = TrueClassFilter.INSTANCE;

}
