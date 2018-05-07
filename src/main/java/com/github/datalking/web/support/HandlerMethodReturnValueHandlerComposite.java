package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.Assert;
import com.github.datalking.web.context.request.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yaoo on 4/29/18
 */
public class HandlerMethodReturnValueHandlerComposite implements HandlerMethodReturnValueHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();

    public List<HandlerMethodReturnValueHandler> getHandlers() {
        return Collections.unmodifiableList(this.returnValueHandlers);
    }

    public boolean supportsReturnType(MethodParameter returnType) {
        return getReturnValueHandler(returnType) != null;
    }

    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  WebRequest webRequest) throws Exception {

        // 获取返回值处理器，jsp时使用 ViewNameMethodReturnValueHandler
        HandlerMethodReturnValueHandler handler = getReturnValueHandler(returnType);

        Assert.notNull(handler, "Unknown return value type [" + returnType.getParameterType().getName() + "]");

        // 处理返回值
        handler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }


    private HandlerMethodReturnValueHandler getReturnValueHandler(MethodParameter returnType) {
        for (HandlerMethodReturnValueHandler returnValueHandler : returnValueHandlers) {

            if (returnValueHandler.supportsReturnType(returnType)) {
                return returnValueHandler;
            }

        }
        return null;
    }

    public HandlerMethodReturnValueHandlerComposite addHandler(HandlerMethodReturnValueHandler returnValuehandler) {
        returnValueHandlers.add(returnValuehandler);
        return this;
    }

    public HandlerMethodReturnValueHandlerComposite addHandlers(
            List<? extends HandlerMethodReturnValueHandler> returnValueHandlers) {
        if (returnValueHandlers != null) {
            for (HandlerMethodReturnValueHandler handler : returnValueHandlers) {
                this.returnValueHandlers.add(handler);
            }
        }
        return this;
    }

}
