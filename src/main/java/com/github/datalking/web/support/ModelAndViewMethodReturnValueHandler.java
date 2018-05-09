package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.mvc.SmartView;
import com.github.datalking.web.mvc.View;

/**
 * @author yaoo on 5/2/18
 */
public class ModelAndViewMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    public boolean supportsReturnType(MethodParameter returnType) {
        return ModelAndView.class.isAssignableFrom(returnType.getParameterType());
    }

    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  WebRequest webRequest) throws Exception {

        if (returnValue == null) {
            mavContainer.setRequestHandled(true);
            return;
        }

        ModelAndView mav = (ModelAndView) returnValue;
        if (mav.isReference()) {
            String viewName = mav.getViewName();
            mavContainer.setViewName(viewName);
            if (viewName != null && viewName.startsWith("redirect:")) {
                mavContainer.setRedirectModelScenario(true);
            }
        } else {
            View view = mav.getView();
            mavContainer.setView(view);
            if (view instanceof SmartView) {
                if (((SmartView) view).isRedirectView()) {
                    mavContainer.setRedirectModelScenario(true);
                }
            }
        }
        mavContainer.addAllAttributes(mav.getModel());
    }

}
