package com.github.datalking.web.bind;

import com.github.datalking.beans.MutablePropertyValues;
import com.github.datalking.web.context.request.WebRequest;

/**
 * @author yaoo on 5/2/18
 */
public class WebRequestDataBinder extends WebDataBinder {

    public WebRequestDataBinder(Object target) {
        super(target);
    }

    public WebRequestDataBinder(Object target, String objectName) {
        super(target, objectName);
    }

    public void bind(WebRequest request) {
//        MutablePropertyValues mpvs = new MutablePropertyValues(request.getParameterMap());
//        if (request instanceof NativeWebRequest) {
//            MultipartRequest multipartRequest = ((NativeWebRequest) request).getNativeRequest(MultipartRequest.class);
//        if (multipartRequest != null) {
//            bindMultipart(multipartRequest.getMultiFileMap(), mpvs);
//        }
//        }
//        doBind(mpvs);
    }


    public void closeNoCatch() throws Exception {
        if (getBindingResult().hasErrors()) {
            throw new Exception(getBindingResult() + "");
        }
    }

}

