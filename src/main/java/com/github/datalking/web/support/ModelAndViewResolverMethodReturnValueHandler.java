package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.mvc.ExtendedModelMap;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.mvc.ModelAndViewResolver;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author yaoo on 5/2/18
 */
public class ModelAndViewResolverMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final List<ModelAndViewResolver> mavResolvers;

    private final ModelAttributeMethodProcessor modelAttributeProcessor = new ModelAttributeMethodProcessor(true);

    public ModelAndViewResolverMethodReturnValueHandler(List<ModelAndViewResolver> mavResolvers) {
        this.mavResolvers = mavResolvers;
    }

    public boolean supportsReturnType(MethodParameter returnType) {
        return true;
    }

    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  WebRequest request) throws Exception {

        if (this.mavResolvers != null) {
            for (ModelAndViewResolver mavResolver : this.mavResolvers) {
                Class<?> handlerType = returnType.getDeclaringClass();
                Method method = returnType.getMethod();
                ExtendedModelMap model = (ExtendedModelMap) mavContainer.getModel();
                ModelAndView mav = mavResolver.resolveModelAndView(method, handlerType, returnValue, model, request);
                if (mav != ModelAndViewResolver.UNRESOLVED) {
                    mavContainer.addAllAttributes(mav.getModel());
                    mavContainer.setViewName(mav.getViewName());
                    if (!mav.isReference()) {
                        mavContainer.setView(mav.getView());
                    }
                    return;
                }
            }
        }

        // No suitable ModelAndViewResolver..

        if (this.modelAttributeProcessor.supportsReturnType(returnType)) {
            this.modelAttributeProcessor.handleReturnValue(returnValue, returnType, mavContainer, request);
        } else {
            throw new UnsupportedOperationException("Unexpected return type: "
                    + returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }

}
