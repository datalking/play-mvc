package com.github.datalking.web.mvc;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yaoo on 5/4/18
 */
public interface LastModified {

    long getLastModified(HttpServletRequest request);

}
