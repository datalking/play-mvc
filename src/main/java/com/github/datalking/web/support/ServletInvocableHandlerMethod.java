package com.github.datalking.web.support;

import com.github.datalking.annotation.web.ResponseStatus;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.web.http.HttpStatus;
import com.github.datalking.web.mvc.View;
import com.github.datalking.web.mvc.method.HandlerMethod;
import com.github.datalking.web.servlet.InvocableHandlerMethod;
import com.github.datalking.web.servlet.ServletWebRequest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author yaoo on 5/2/18
 */
public class ServletInvocableHandlerMethod extends InvocableHandlerMethod {

    private HttpStatus responseStatus;

    private String responseReason;

    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    public ServletInvocableHandlerMethod(Object handler, Method method) {
        super(handler, method);
        initResponseStatus();
    }

    public ServletInvocableHandlerMethod(HandlerMethod handlerMethod) {
        super(handlerMethod);
        initResponseStatus();
    }

    private void initResponseStatus() {
        ResponseStatus annot = getMethodAnnotation(ResponseStatus.class);
        if (annot != null) {
            this.responseStatus = annot.value();
            this.responseReason = annot.reason();
        }
    }

    public void setHandlerMethodReturnValueHandlers(HandlerMethodReturnValueHandlerComposite returnValueHandlers) {
        this.returnValueHandlers = returnValueHandlers;
    }

    public final void invokeAndHandle(ServletWebRequest webRequest,
                                      ModelAndViewContainer mavContainer,
                                      Object... providedArgs) throws Exception {

        // ==== 调用处理请求的方法
        Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);

        setResponseStatus(webRequest);

        if (returnValue == null) {
            if (isRequestNotModified(webRequest) || hasResponseStatus() || mavContainer.isRequestHandled()) {
                mavContainer.setRequestHandled(true);
                return;
            }
        } else if (StringUtils.hasText(this.responseReason)) {
            mavContainer.setRequestHandled(true);
            return;
        }

        mavContainer.setRequestHandled(false);

        try {
            MethodParameter returnValType = getReturnValueType(returnValue);
            this.returnValueHandlers.handleReturnValue(returnValue, returnValType, mavContainer, webRequest);
        } catch (Exception ex) {
            if (logger.isTraceEnabled()) {
                logger.trace(getReturnValueHandlingErrorMessage("Error handling return value", returnValue), ex);
            }
            throw ex;
        }
    }

    private void setResponseStatus(ServletWebRequest webRequest) throws IOException {
        if (this.responseStatus == null) {
            return;
        }

        if (StringUtils.hasText(this.responseReason)) {
            webRequest.getResponse().sendError(this.responseStatus.value(), this.responseReason);
        } else {
            webRequest.getResponse().setStatus(this.responseStatus.value());
        }

        // to be picked up by the RedirectView
        webRequest.getRequest().setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, this.responseStatus);
    }

    private boolean isRequestNotModified(ServletWebRequest webRequest) {
        return webRequest.isNotModified();
    }

    private boolean hasResponseStatus() {
        return responseStatus != null;
    }

    private String getReturnValueHandlingErrorMessage(String message, Object returnValue) {
        StringBuilder sb = new StringBuilder(message);
        if (returnValue != null) {
            sb.append(" [type=" + returnValue.getClass().getName() + "] ");
        }
        sb.append("[value=" + returnValue + "]");
        return getDetailedErrorMessage(sb.toString());
    }

    ServletInvocableHandlerMethod wrapConcurrentResult(final Object result) {

        return new CallableHandlerMethod(new Callable<Object>() {

            public Object call() throws Exception {
                if (result instanceof Exception) {
                    throw (Exception) result;
                } else if (result instanceof Throwable) {
                    throw new Exception("Async processing failed", (Throwable) result);
                }
                return result;
            }
        });
    }


    private class CallableHandlerMethod extends ServletInvocableHandlerMethod {

        public CallableHandlerMethod(Callable<?> callable) {
            super(callable, ClassUtils.getMethod(callable.getClass(), "call"));
            this.setHandlerMethodReturnValueHandlers(ServletInvocableHandlerMethod.this.returnValueHandlers);
        }

        @Override
        public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
            return ServletInvocableHandlerMethod.this.getMethodAnnotation(annotationType);
        }
    }

}
