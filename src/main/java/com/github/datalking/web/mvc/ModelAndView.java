package com.github.datalking.web.mvc;

import com.github.datalking.util.CollectionUtils;

import java.util.Map;

/**
 * 存放model和view
 *
 * @author yaoo on 4/25/18
 */
public class ModelAndView {

    private Object view;

    private ModelMap model;

    private boolean cleared = false;

    public ModelAndView() {
    }

    public ModelAndView(View view) {
        this.view = view;
    }

    public ModelAndView(String viewName, Map<String, ?> model) {
        this.view = viewName;
        if (model != null) {
            getModelMap().addAllAttributes(model);
        }
    }

    public ModelAndView(View view, Map<String, ?> model) {
        this.view = view;
        if (model != null) {
            getModelMap().addAllAttributes(model);
        }
    }

    public ModelAndView(String viewName, String modelName, Object modelObject) {
        this.view = viewName;
        addObject(modelName, modelObject);
    }

    public ModelAndView(View view, String modelName, Object modelObject) {
        this.view = view;
        addObject(modelName, modelObject);
    }

    public void setViewName(String viewName) {
        this.view = viewName;
    }

    public String getViewName() {
        return (this.view instanceof String ? (String) this.view : null);
    }

    public void setView(View view) {
        this.view = view;
    }

    public View getView() {
        return (this.view instanceof View ? (View) this.view : null);
    }

    public boolean hasView() {
        return (this.view != null);
    }

    public boolean isReference() {
        return (this.view instanceof String);
    }

    public Map<String, Object> getModelInternal() {
        return this.model;
    }

    public ModelMap getModelMap() {
        if (this.model == null) {
            this.model = new ModelMap();
        }
        return this.model;
    }

    public Map<String, Object> getModel() {
        return getModelMap();
    }

    public ModelAndView addObject(String attributeName, Object attributeValue) {
        getModelMap().addAttribute(attributeName, attributeValue);
        return this;
    }

    public ModelAndView addObject(Object attributeValue) {
        getModelMap().addAttribute(attributeValue);
        return this;
    }

    public ModelAndView addAllObjects(Map<String, ?> modelMap) {
        getModelMap().addAllAttributes(modelMap);
        return this;
    }

    public void clear() {
        this.view = null;
        this.model = null;
        this.cleared = true;
    }

    public boolean isEmpty() {
        return (this.view == null && CollectionUtils.isEmpty(this.model));
    }

    public boolean wasCleared() {
        return (this.cleared && isEmpty());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ModelAndView: ");
        if (isReference()) {
            sb.append("reference to view with name '").append(this.view).append("'");
        } else {
            sb.append("materialized View is [").append(this.view).append(']');
        }
        sb.append("; model is ").append(this.model);
        return sb.toString();
    }

}
