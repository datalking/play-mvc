package com.github.datalking.web.support;

import com.github.datalking.beans.factory.DisposableBean;
import com.github.datalking.beans.factory.ObjectFactory;
import com.github.datalking.beans.factory.config.Scope;
import com.github.datalking.util.Assert;

import javax.servlet.ServletContext;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author yaoo on 6/3/18
 */
public class ServletContextScope implements Scope, DisposableBean {

    private final ServletContext servletContext;

    private final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<>();

    public ServletContextScope(ServletContext servletContext) {
        Assert.notNull(servletContext, "ServletContext must not be null");
        this.servletContext = servletContext;
    }


    public Object get(String name, ObjectFactory<?> objectFactory) {
        Object scopedObject = this.servletContext.getAttribute(name);
        if (scopedObject == null) {
            scopedObject = objectFactory.getObject();
            this.servletContext.setAttribute(name, scopedObject);
        }
        return scopedObject;
    }

    public Object remove(String name) {
        Object scopedObject = this.servletContext.getAttribute(name);
        if (scopedObject != null) {
            this.servletContext.removeAttribute(name);
            this.destructionCallbacks.remove(name);
            return scopedObject;
        } else {
            return null;
        }
    }

    public void registerDestructionCallback(String name, Runnable callback) {
        this.destructionCallbacks.put(name, callback);
    }

    public Object resolveContextualObject(String key) {
        return null;
    }

    public String getConversationId() {
        return null;
    }

    public void destroy() {
        for (Runnable runnable : this.destructionCallbacks.values()) {
            runnable.run();
        }
        this.destructionCallbacks.clear();
    }

}
