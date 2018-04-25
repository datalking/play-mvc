package com.github.datalking.web.servlet;

import com.github.datalking.beans.MutablePropertyValues;
import com.github.datalking.beans.PropertyValue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yaoo on 4/25/18
 */
public class ServletConfigPropertyValues extends MutablePropertyValues {


    public ServletConfigPropertyValues(ServletConfig config, Set<String> requiredProperties) throws ServletException {

        Set<String> missingProps = (requiredProperties != null && !requiredProperties.isEmpty()) ? new HashSet<>(requiredProperties) : null;

        Enumeration en = config.getInitParameterNames();
        while (en.hasMoreElements()) {
            String property = (String) en.nextElement();
            Object value = config.getInitParameter(property);
            addPropertyValue(new PropertyValue(property, value));
            if (missingProps != null) {
                missingProps.remove(property);
            }
        }

        if (missingProps != null && missingProps.size() > 0) {
            throw new ServletException("Initialization from ServletConfig for servlet '" + config.getServletName() +
                    "' failed; the following required properties were missing: " + ", ");
        }
    }
}
