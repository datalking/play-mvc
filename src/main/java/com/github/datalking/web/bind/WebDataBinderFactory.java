package com.github.datalking.web.bind;

import com.github.datalking.web.context.request.WebRequest;

/**
 * @author yaoo on 4/29/18
 */
public interface WebDataBinderFactory {

    WebDataBinder createBinder(WebRequest webRequest, Object target, String objectName) throws Exception;

}
