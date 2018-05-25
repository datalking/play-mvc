package com.github.datalking.web.support;

import com.github.datalking.web.bind.BindingAwareModelMap;
import com.github.datalking.web.mvc.ModelMap;

import java.util.Map;

/**
 * @author yaoo on 4/29/18
 */
public class ModelAndViewContainer {

    private boolean ignoreDefaultModelOnRedirect = false;

    private Object view;

    private final ModelMap defaultModel = new BindingAwareModelMap();

    private ModelMap redirectModel;

    private boolean redirectModelScenario = false;

    private final SessionStatus sessionStatus = new SimpleSessionStatus();

    private boolean requestHandled = false;

    public void setIgnoreDefaultModelOnRedirect(boolean ignoreDefaultModelOnRedirect) {
        this.ignoreDefaultModelOnRedirect = ignoreDefaultModelOnRedirect;
    }

    public void setViewName(String viewName) {
        this.view = viewName;
    }

    public String getViewName() {
        return (this.view instanceof String ? (String) this.view : null);
    }

    public void setView(Object view) {
        this.view = view;
    }

    public Object getView() {
        return this.view;
    }

    public boolean isViewReference() {
        return (this.view instanceof String);
    }

    public ModelMap getModel() {
        if (useDefaultModel()) {
            return this.defaultModel;
        } else {
            if (this.redirectModel == null) {
                this.redirectModel = new ModelMap();
            }
            return this.redirectModel;
        }
    }

    private boolean useDefaultModel() {
        return (!this.redirectModelScenario || (this.redirectModel == null && !this.ignoreDefaultModelOnRedirect));
    }

    public void setRedirectModel(ModelMap redirectModel) {
        this.redirectModel = redirectModel;
    }

    public void setRedirectModelScenario(boolean redirectModelScenario) {
        this.redirectModelScenario = redirectModelScenario;
    }

    public SessionStatus getSessionStatus() {
        return this.sessionStatus;
    }

    public void setRequestHandled(boolean requestHandled) {
        this.requestHandled = requestHandled;
    }

    public boolean isRequestHandled() {
        return this.requestHandled;
    }

    public ModelAndViewContainer addAttribute(String name, Object value) {
        getModel().addAttribute(name, value);
        return this;
    }

    public ModelAndViewContainer addAttribute(Object value) {
        getModel().addAttribute(value);
        return this;
    }

    public ModelAndViewContainer addAllAttributes(Map<String, ?> attributes) {
        getModel().addAllAttributes(attributes);
        return this;
    }

    public ModelAndViewContainer mergeAttributes(Map<String, ?> attributes) {
        getModel().mergeAttributes(attributes);
        return this;
    }

    public ModelAndViewContainer removeAttributes(Map<String, ?> attributes) {
        if (attributes != null) {
            for (String key : attributes.keySet()) {
                getModel().remove(key);
            }
        }
        return this;
    }

    public boolean containsAttribute(String name) {
        return getModel().containsAttribute(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ModelAndViewContainer: ");
        if (!isRequestHandled()) {
            if (isViewReference()) {
                sb.append("reference to view with name '").append(this.view).append("'");
            } else {
                sb.append("View is [").append(this.view).append(']');
            }
            if (useDefaultModel()) {
                sb.append("; default model ");
            } else {
                sb.append("; redirect model ");
            }
            sb.append(getModel());
        } else {
            sb.append("Request handled directly");
        }
        return sb.toString();
    }

}
