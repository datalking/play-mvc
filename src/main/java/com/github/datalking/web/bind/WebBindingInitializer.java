package com.github.datalking.web.bind;

import com.github.datalking.web.context.request.WebRequest;

/**
 * @author yaoo on 4/29/18
 */
public interface WebBindingInitializer {

    void initBinder(WebDataBinder binder, WebRequest request);

}
