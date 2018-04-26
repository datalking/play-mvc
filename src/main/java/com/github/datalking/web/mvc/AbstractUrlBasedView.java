package com.github.datalking.web.mvc;

import com.github.datalking.beans.factory.InitializingBean;

import java.util.Locale;

/**
 * @author yaoo on 4/26/18
 */
public abstract class AbstractUrlBasedView extends AbstractView implements InitializingBean {

    private String url;

    protected AbstractUrlBasedView() {
    }

    protected AbstractUrlBasedView(String url) {
        this.url = url;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void afterPropertiesSet() {
        if (isUrlRequired() && getUrl() == null) {
            throw new IllegalArgumentException("Property 'url' is required");
        }
    }

    protected boolean isUrlRequired() {
        return true;
    }

    public boolean checkResource(Locale locale) throws Exception {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("; URL [").append(getUrl()).append("]");
        return sb.toString();
    }

}
