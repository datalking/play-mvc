package com.github.datalking.web.mvc;

import com.github.datalking.web.context.request.WebRequest;

import java.lang.reflect.Method;

/**
 * @author yaoo on 4/29/18
 */
public interface ModelAndViewResolver {

    ModelAndView UNRESOLVED = new ModelAndView();

    ModelAndView resolveModelAndView(Method handlerMethod,
                                     Class<?> handlerType,
                                     Object returnValue,
                                     ExtendedModelMap implicitModel,
                                     WebRequest webRequest);

}
