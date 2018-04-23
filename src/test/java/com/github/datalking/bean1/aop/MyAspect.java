package com.github.datalking.bean1.aop;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * @author yaoo on 4/10/18
 */

@Aspect
public class MyAspect {

    @Before("execution(* com.github.datalking.bean..*.*(..))")
    public void before1() {

        System.out.println("====print before1");
    }

    @After("execution(* com.github.datalking.bean..*.*(..))")
    public void after1() {

        System.out.println("====print after1");
    }

}

