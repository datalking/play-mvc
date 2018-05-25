package com.github.datalking.web.bind;

import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.servlet.InvocableHandlerMethod;

import java.util.List;

/**
 * @author yaoo on 5/2/18
 */
public class ServletRequestDataBinderFactory extends InitBinderDataBinderFactory {

    public ServletRequestDataBinderFactory(List<InvocableHandlerMethod> binderMethods, WebBindingInitializer initializer) {
        super(binderMethods, initializer);
    }

    @Override
    protected ServletRequestDataBinder createBinderInstance(Object target, String objectName, WebRequest request) {

        return new ExtendedServletRequestDataBinder(target, objectName);
    }

}
