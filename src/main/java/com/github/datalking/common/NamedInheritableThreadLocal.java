package com.github.datalking.common;

import com.github.datalking.util.Assert;

/**
 * @author yaoo on 4/25/18
 */
public class NamedInheritableThreadLocal<T> extends InheritableThreadLocal<T> {

    private final String name;

    public NamedInheritableThreadLocal(String name) {
        Assert.notNull(name, "Name must not be empty");
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
