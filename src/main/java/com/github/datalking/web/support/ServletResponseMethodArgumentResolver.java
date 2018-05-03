package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.web.bind.WebDataBinderFactory;
import com.github.datalking.web.context.request.WebRequest;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Method;

/**
 * @author yaoo on 5/3/18
 */
public class ServletResponseMethodArgumentResolver implements HandlerMethodArgumentResolver {

    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();
        return ServletResponse.class.isAssignableFrom(paramType)
                || OutputStream.class.isAssignableFrom(paramType)
                || Writer.class.isAssignableFrom(paramType);
    }
    public Object resolveArgument(
            MethodParameter parameter, ModelAndViewContainer mavContainer,
            WebRequest webRequest, WebDataBinderFactory binderFactory)
            throws IOException {

        if (mavContainer != null) {
            mavContainer.setRequestHandled(true);
        }

        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        Class<?> paramType = parameter.getParameterType();

        if (ServletResponse.class.isAssignableFrom(paramType)) {
            Object nativeResponse = webRequest.getNativeResponse(paramType);
            if (nativeResponse == null) {
                throw new IllegalStateException(
                        "Current response is not of type [" + paramType.getName() + "]: " + response);
            }
            return nativeResponse;
        }
        else if (OutputStream.class.isAssignableFrom(paramType)) {
            return response.getOutputStream();
        }
        else if (Writer.class.isAssignableFrom(paramType)) {
            return response.getWriter();
        }
        else {
            // should not happen
            Method method = parameter.getMethod();
            throw new UnsupportedOperationException("Unknown parameter type: " + paramType + " in method: " + method);
        }
    }

}
