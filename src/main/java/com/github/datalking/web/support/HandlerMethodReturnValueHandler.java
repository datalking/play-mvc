package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.web.context.request.WebRequest;

/**
 * @author yaoo on 4/26/18
 */
public interface HandlerMethodReturnValueHandler {

    boolean supportsReturnType(MethodParameter returnType);

    void handleReturnValue(Object returnValue,
                           MethodParameter returnType,
                           ModelAndViewContainer mavContainer,
                           WebRequest webRequest) throws Exception;


}
