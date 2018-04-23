package com.github.datalking.aop;

import java.io.Serializable;

/**
 * @author yaoo on 4/18/18
 */
public class EmptyTargetSource implements TargetSource, Serializable {

    private final Class<?> targetClass;

    public static final EmptyTargetSource INSTANCE = new EmptyTargetSource(null);

    public EmptyTargetSource(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    @Override
    public Object getTarget() {
        return null;
    }


    @Override
    public String toString() {
        return "EmptyTargetSource{" +
                "targetClass=" + targetClass +
                '}';
    }


}
