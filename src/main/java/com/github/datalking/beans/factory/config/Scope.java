package com.github.datalking.beans.factory.config;

import com.github.datalking.beans.factory.ObjectFactory;

/**
 * @author yaoo on 4/29/18
 */
public interface Scope {

    Object get(String name, ObjectFactory<?> objectFactory);

    Object remove(String name);

    void registerDestructionCallback(String name, Runnable callback);

    Object resolveContextualObject(String key);

    String getConversationId();

}
