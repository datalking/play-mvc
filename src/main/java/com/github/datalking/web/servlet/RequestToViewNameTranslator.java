package com.github.datalking.web.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yaoo on 4/25/18
 */
public interface RequestToViewNameTranslator {

    // 将HttpServletRequest翻译成view名
    String getViewName(HttpServletRequest request);

}
