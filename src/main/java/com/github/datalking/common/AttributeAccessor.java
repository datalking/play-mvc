package com.github.datalking.common;

/**
 * @author yaoo on 5/29/18
 */
public interface AttributeAccessor {

    Object getAttribute(String name);

    void setAttribute(String name, Object value);

    Object removeAttribute(String name);

    boolean hasAttribute(String name);

    String[] attributeNames();

}
