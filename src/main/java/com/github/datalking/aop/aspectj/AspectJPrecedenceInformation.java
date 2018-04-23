package com.github.datalking.aop.aspectj;

/**
 * @author yaoo on 4/19/18
 */
public interface AspectJPrecedenceInformation {

    String getAspectName();

    int getDeclarationOrder();

    boolean isBeforeAdvice();

    boolean isAfterAdvice();

}
