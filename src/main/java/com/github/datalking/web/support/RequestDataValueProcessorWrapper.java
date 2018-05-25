package com.github.datalking.web.support;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yaoo on 4/26/18
 */
public class RequestDataValueProcessorWrapper implements RequestDataValueProcessor {

    private RequestDataValueProcessor processor;

    public void setRequestDataValueProcessor(RequestDataValueProcessor processor) {
        this.processor = processor;
    }

    @Override
    public String processUrl(HttpServletRequest request, String url) {
        return (this.processor != null) ? this.processor.processUrl(request, url) : url;
    }

    @Override
    public String processFormFieldValue(HttpServletRequest request, String name, String value, String type) {
        return (this.processor != null) ? this.processor.processFormFieldValue(request, name, value, type) : value;
    }

    @Override
    public String processAction(HttpServletRequest request, String action) {
        return (this.processor != null) ? this.processor.processAction(request, action) : action;
    }

    @Override
    public Map<String, String> getExtraHiddenFields(HttpServletRequest request) {
        return (this.processor != null) ? this.processor.getExtraHiddenFields(request) : null;
    }

}
