package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.web.bind.WebDataBinderFactory;
import com.github.datalking.web.context.request.WebRequest;

/**
 * 方法参数值解析 接口
 *
 * @author yaoo on 4/26/18
 */
public interface HandlerMethodArgumentResolver {

    // 判断方法参数是否包含指定的参数注解
    boolean supportsParameter(MethodParameter parameter);

    // 在给定的具体的请求中，把方法的参数解析到参数值里面，返回解析到的参数值，没有返回nul
    Object resolveArgument(MethodParameter parameter,
                           ModelAndViewContainer mavContainer,
                           WebRequest webRequest,
                           WebDataBinderFactory binderFactory) throws Exception;

}
