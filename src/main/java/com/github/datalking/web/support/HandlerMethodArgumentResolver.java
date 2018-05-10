package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.web.bind.WebDataBinderFactory;
import com.github.datalking.web.context.request.WebRequest;

/**
 * @author yaoo on 4/26/18
 */
public interface HandlerMethodArgumentResolver {

    boolean supportsParameter(MethodParameter parameter);

    Object resolveArgument(MethodParameter parameter,
                           ModelAndViewContainer mavContainer,
                           WebRequest webRequest,
                           WebDataBinderFactory binderFactory) throws Exception;

}
