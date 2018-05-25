package com.github.datalking.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 类似于HttpServlet，仅有保留一个接口用于处理http请求
 *
 * @author yaoo on 5/4/18
 */
public interface HttpRequestHandler {

    void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
