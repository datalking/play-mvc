package com.github.datalking.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * mvc的view
 *
 * @author yaoo on 4/25/18
 */
public interface View {

    String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";
    String PATH_VARIABLES = View.class.getName() + ".pathVariables";
    String SELECTED_CONTENT_TYPE = View.class.getName() + ".selectedContentType";

    String getContentType();

    // 根据model渲染view
    void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response);


}
