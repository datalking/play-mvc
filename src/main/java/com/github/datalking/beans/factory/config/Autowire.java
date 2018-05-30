package com.github.datalking.beans.factory.config;

/**
 * @author yaoo on 5/30/18
 */
public enum Autowire {

    NO(AutowireCapableBeanFactory.AUTOWIRE_NO),

    BY_NAME(AutowireCapableBeanFactory.AUTOWIRE_BY_NAME),

    BY_TYPE(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);


    private final int value;

    Autowire(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    /**
     * Return whether this represents an actual autowiring value.
     *
     * @return whether actual autowiring was specified (either BY_NAME or BY_TYPE)
     */
    public boolean isAutowire() {
        return (this == BY_NAME || this == BY_TYPE);
    }

}
