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

        HandlerMethodReturnValueHandler handler = getReturnValueHandler(returnType);
        Assert.notNull(handler, "Unknown return value type [" + returnType.getParameterType().getName() + "]");

        handler.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }


    private HandlerMethodReturnValueHandler getReturnValueHandler(MethodParameter returnType) {
        for (HandlerMethodReturnValueHandler returnValueHandler : returnValueHandlers) {
            if (logger.isTraceEnabled()) {
                logger.trace("Testing if return value handler [" + returnValueHandler + "] supports [" +
                        returnType.getGenericParameterType() + "]");
            }
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
