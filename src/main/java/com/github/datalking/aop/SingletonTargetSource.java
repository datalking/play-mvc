package com.github.datalking.aop;

import com.github.datalking.util.Assert;

import java.io.Serializable;

public class SingletonTargetSource implements TargetSource, Serializable {

    private final Object target;

    public SingletonTargetSource(Object target) {
        Assert.notNull(target, "Target object must not be null");
        this.target = target;
    }

    @Override
    public Class<?> getTargetClass() {
        return this.target.getClass();
    }

    @Override
    public Object getTarget() {
        return this.target;
    }

    //@Override
    public void releaseTarget(Object target) {
        // nothing to do
    }

    //@Override
    public boolean isStatic() {
        return true;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SingletonTargetSource)) {
            return false;
        }
        SingletonTargetSource otherTargetSource = (SingletonTargetSource) other;
        return this.target.equals(otherTargetSource.target);
    }


    @Override
    public int hashCode() {
        return this.target.hashCode();
    }

    @Override
    public String toString() {
        return "SingletonTargetSource for [" + this.target + "]";
    }

}
