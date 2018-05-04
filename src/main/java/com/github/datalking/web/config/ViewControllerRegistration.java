package com.github.datalking.web.config;

import com.github.datalking.util.Assert;
import com.github.datalking.web.mvc.ParameterizableViewController;

/**
 * @author yaoo on 5/4/18
 */
public class ViewControllerRegistration {

    private final String urlPath;

    private String viewName;

    public ViewControllerRegistration(String urlPath) {
        Assert.notNull(urlPath, "A URL path is required to create a view controller.");
        this.urlPath = urlPath;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    protected String getUrlPath() {
        return urlPath;
    }

    protected Object getViewController() {
        ParameterizableViewController controller = new ParameterizableViewController();
        controller.setViewName(viewName);
        return controller;
    }

}
