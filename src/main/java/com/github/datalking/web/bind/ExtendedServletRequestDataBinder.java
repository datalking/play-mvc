package com.github.datalking.web.bind;

import com.github.datalking.beans.MutablePropertyValues;
import com.github.datalking.web.servlet.HandlerMapping;

import javax.servlet.ServletRequest;
import java.util.Map;

/**
 * @author yaoo on 5/2/18
 */
public class ExtendedServletRequestDataBinder extends ServletRequestDataBinder {

    public ExtendedServletRequestDataBinder(Object target) {
        super(target);
    }

    public ExtendedServletRequestDataBinder(Object target, String objectName) {
        super(target, objectName);
    }

    @Override
    protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
        String attr = HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
        Map<String, String> uriVars = (Map<String, String>) request.getAttribute(attr);
        if (uriVars != null) {
            for (Map.Entry<String, String> entry : uriVars.entrySet()) {
                if (mpvs.contains(entry.getKey())) {
                    logger.warn("Skipping URI variable '" + entry.getKey() + "' since request contains a value with same name.");
                } else {
                    mpvs.addPropertyValue(entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
