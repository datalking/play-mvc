package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;

/**
 * @author yaoo on 4/26/18
 */
public interface HandlerMethodArgumentResolver {

    boolean supportsParameter(MethodParameter parameter);

    Object resolveArgument(MethodParameter parameter,
                           ModelAndViewContainer mavContainer,
                           NativeWebRequest webRequest,
                           WebDataBinderFactory binderFactory) throws Exception;


}
