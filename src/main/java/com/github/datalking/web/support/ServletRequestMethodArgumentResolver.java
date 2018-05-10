package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.util.web.RequestContextUtils;
import com.github.datalking.web.bind.WebDataBinderFactory;
import com.github.datalking.web.context.request.WebRequest;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.Principal;
import java.util.Locale;

/**
 * 请求参数解析相关方法
 *
 * @author yaoo on 5/3/18
 */
public class ServletRequestMethodArgumentResolver implements HandlerMethodArgumentResolver {

    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> paramType = parameter.getParameterType();
        return WebRequest.class.isAssignableFrom(paramType) ||
                ServletRequest.class.isAssignableFrom(paramType) ||
//                MultipartRequest.class.isAssignableFrom(paramType) ||
                HttpSession.class.isAssignableFrom(paramType) ||
                Principal.class.isAssignableFrom(paramType) ||
                Locale.class.equals(paramType) ||
                InputStream.class.isAssignableFrom(paramType) ||
                Reader.class.isAssignableFrom(paramType);
    }

    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  WebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws IOException {

        Class<?> paramType = parameter.getParameterType();
        if (WebRequest.class.isAssignableFrom(paramType)) {
            return webRequest;
        }

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

//        if (ServletRequest.class.isAssignableFrom(paramType) || MultipartRequest.class.isAssignableFrom(paramType)) {
        if (ServletRequest.class.isAssignableFrom(paramType)) {
            Object nativeRequest = webRequest.getNativeRequest(paramType);
            if (nativeRequest == null) {
                throw new IllegalStateException("Current request is not of type [" + paramType.getName() + "]: " + request);
            }

            return nativeRequest;
        } else if (HttpSession.class.isAssignableFrom(paramType)) {

            return request.getSession();
        } else if (Principal.class.isAssignableFrom(paramType)) {

            return request.getUserPrincipal();
//        } else if (Locale.class.equals(paramType)) {
//            return RequestContextUtils.getLocale(request);
        } else if (InputStream.class.isAssignableFrom(paramType)) {

            return request.getInputStream();
        } else if (Reader.class.isAssignableFrom(paramType)) {

            return request.getReader();
        } else {
            // should never happen...
            throw new UnsupportedOperationException("Unknown parameter type: " + paramType + " in method: " + parameter.getMethod());
        }
    }

}
