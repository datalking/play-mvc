package com.github.datalking.web.support;

import com.github.datalking.util.Assert;
import com.github.datalking.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yaoo on 4/26/18
 */
public class ContextExposingHttpServletRequest extends HttpServletRequestWrapper {

    private final WebApplicationContext webApplicationContext;

    private final Set<String> exposedContextBeanNames;

    private Set<String> explicitAttributes;


    public ContextExposingHttpServletRequest(HttpServletRequest originalRequest, WebApplicationContext context) {
        this(originalRequest, context, null);
    }


    public ContextExposingHttpServletRequest(HttpServletRequest originalRequest, WebApplicationContext context, Set<String> exposedContextBeanNames) {

        super(originalRequest);
        Assert.notNull(context, "WebApplicationContext must not be null");
        this.webApplicationContext = context;
        this.exposedContextBeanNames = exposedContextBeanNames;
    }


    public final WebApplicationContext getWebApplicationContext() {
        return this.webApplicationContext;
    }


    @Override
    public Object getAttribute(String name) {
        if ((this.explicitAttributes == null || !this.explicitAttributes.contains(name)) &&
                (this.exposedContextBeanNames == null || this.exposedContextBeanNames.contains(name)) &&
                this.webApplicationContext.containsBean(name)) {
            return this.webApplicationContext.getBean(name);
        } else {
            return super.getAttribute(name);
        }
    }

    @Override
    public void setAttribute(String name, Object value) {
        super.setAttribute(name, value);
        if (this.explicitAttributes == null) {
            this.explicitAttributes = new HashSet<String>(8);
        }
        this.explicitAttributes.add(name);
    }

}
