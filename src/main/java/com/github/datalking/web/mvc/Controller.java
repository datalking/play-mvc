package com.github.datalking.web.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yaoo on 4/25/18
 */
public interface Controller {

    ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
