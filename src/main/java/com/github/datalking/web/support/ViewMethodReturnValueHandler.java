package com.github.datalking.web.support;

import com.github.datalking.common.MethodParameter;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.mvc.SmartView;
import com.github.datalking.web.mvc.View;

/**
 * @author yaoo on 5/2/18
 */
public class ViewMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    public boolean supportsReturnType(MethodParameter returnType) {
        return View.class.isAssignableFrom(returnType.getParameterType());
    }

    public void handleReturnValue(
            Object returnValue, MethodParameter returnType,
            ModelAndViewContainer mavContainer, WebRequest webRequest)
            throws Exception {

        if (returnValue == null) {
            return;
        } else if (returnValue instanceof View) {
            View view = (View) returnValue;
            mavContainer.setView(view);
            if (view instanceof SmartView) {
                if (((SmartView) view).isRedirectView()) {
                    mavContainer.setRedirectModelScenario(true);
                }
            }
        } else {
            // should not happen
            throw new UnsupportedOperationException("Unexpected return type: " +
                    returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }

}
