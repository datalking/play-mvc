package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.web.context.request.WebRequest;

/**
 * @author yaoo on 5/2/18
 */
public class ViewNameMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {

        Class<?> paramType = returnType.getParameterType();

        return (void.class.equals(paramType) || String.class.equals(paramType));
    }

    @Override
    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  WebRequest webRequest) throws Exception {

        if (returnValue == null) {
            return;
        } else if (returnValue instanceof String) {
            String viewName = returnValue.toString();
            mavContainer.setViewName(viewName);

            if (isRedirectViewName(viewName)) {
                mavContainer.setRedirectModelScenario(true);
            }

        } else {
            throw new UnsupportedOperationException("Unexpected return type: " + returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }

    protected boolean isRedirectViewName(String viewName) {
        return viewName.startsWith("redirect:");
    }

}

