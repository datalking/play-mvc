package com.github.datalking.aop.framework;

import java.lang.reflect.Method;

/**
 * 方法包装类，便于快速比较方法
 *
 * @author yaoo on 4/19/18
 */
public class MethodCacheKey implements Comparable<MethodCacheKey> {

    private final Method method;

    private final int hashCode;

    public MethodCacheKey(Method method) {
        this.method = method;
        this.hashCode = method.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof MethodCacheKey &&
                this.method == ((MethodCacheKey) other).method));
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return this.method.toString();
    }

    @Override
    public int compareTo(MethodCacheKey other) {
        int result = this.method.getName().compareTo(other.method.getName());
        if (result == 0) {
            result = this.method.toString().compareTo(other.method.toString());
        }
        return result;
    }


}
