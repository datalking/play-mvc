package com.github.datalking.web.support;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author yaoo on 4/26/18
 */
public interface RequestDataValueProcessor {

    String processUrl(HttpServletRequest request, String url);

    String processAction(HttpServletRequest request, String action);

    String processFormFieldValue(HttpServletRequest request, String name, String value, String type);

    Map<String, String> getExtraHiddenFields(HttpServletRequest request);

}
