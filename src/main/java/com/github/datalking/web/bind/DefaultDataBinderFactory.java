package com.github.datalking.web.bind;

import com.github.datalking.web.context.request.WebRequest;

/**
 * @author yaoo on 5/2/18
 */
public class DefaultDataBinderFactory implements WebDataBinderFactory {

    private final WebBindingInitializer initializer;

    public DefaultDataBinderFactory(WebBindingInitializer initializer) {
        this.initializer = initializer;
    }

    public final WebDataBinder createBinder(WebRequest webRequest, Object target, String objectName)
            throws Exception {
        WebDataBinder dataBinder = createBinderInstance(target, objectName, webRequest);
        if (this.initializer != null) {
            this.initializer.initBinder(dataBinder, webRequest);
        }
        initBinder(dataBinder, webRequest);
        return dataBinder;
    }

    protected WebDataBinder createBinderInstance(Object target, String objectName, WebRequest webRequest)
            throws Exception {
        return new WebRequestDataBinder(target, objectName);
    }

    protected void initBinder(WebDataBinder dataBinder, WebRequest webRequest) throws Exception {
    }

}
