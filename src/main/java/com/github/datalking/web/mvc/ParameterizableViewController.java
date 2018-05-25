package com.github.datalking.web.mvc;

import com.github.datalking.util.web.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yaoo on 5/4/18
 */
public class ParameterizableViewController extends AbstractController {

    private String viewName;

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return this.viewName;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView(getViewName(), RequestContextUtils.getInputFlashMap(request));
    }

}
