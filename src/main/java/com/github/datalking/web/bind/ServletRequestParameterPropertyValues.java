package com.github.datalking.web.bind;

import com.github.datalking.beans.MutablePropertyValues;
import com.github.datalking.util.web.WebUtils;

import javax.servlet.ServletRequest;

/**
 * @author yaoo on 5/10/18
 */
public class ServletRequestParameterPropertyValues extends MutablePropertyValues {

    public static final String DEFAULT_PREFIX_SEPARATOR = "_";

    public ServletRequestParameterPropertyValues(ServletRequest request) {
        this(request, null, null);
    }

    public ServletRequestParameterPropertyValues(ServletRequest request, String prefix) {
        this(request, prefix, DEFAULT_PREFIX_SEPARATOR);
    }

    public ServletRequestParameterPropertyValues(ServletRequest request, String prefix, String prefixSeparator) {

        super(WebUtils.getParametersStartingWith(request, (prefix != null ? prefix + prefixSeparator : null)));
    }


}
