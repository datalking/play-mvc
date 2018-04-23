package com.github.datalking.common;

import com.github.datalking.util.Assert;

/**
 * @author yaoo on 4/19/18
 */
public class NamedThreadLocal<T> extends ThreadLocal<T> {

    private final String name;

    public NamedThreadLocal(String name) {
        Assert.notNull(name, "Name must not be empty");
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
