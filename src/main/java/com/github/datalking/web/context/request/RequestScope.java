package com.github.datalking.web.context.request;

import com.github.datalking.web.http.RequestAttributes;

/**
 * @author yaoo on 4/29/18
 */
public class RequestScope extends AbstractRequestAttributesScope {

    @Override
    protected int getScope() {
        return RequestAttributes.SCOPE_REQUEST;
    }

    public String getConversationId() {
        return null;
    }

}

