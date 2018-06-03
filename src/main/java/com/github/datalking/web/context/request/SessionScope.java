package com.github.datalking.web.context.request;

import com.github.datalking.beans.factory.ObjectFactory;
import com.github.datalking.web.http.RequestAttributes;

/**
 * @author yaoo on 6/3/18
 */
public class SessionScope extends AbstractRequestAttributesScope {

    private final int scope;

    public SessionScope() {
        this.scope = RequestAttributes.SCOPE_SESSION;
    }

    public SessionScope(boolean globalSession) {
        this.scope = (globalSession ? RequestAttributes.SCOPE_GLOBAL_SESSION : RequestAttributes.SCOPE_SESSION);
    }


    @Override
    protected int getScope() {
        return this.scope;
    }

    public String getConversationId() {
        return RequestContextHolder.currentRequestAttributes().getSessionId();
    }

    @Override
    public Object get(String name, ObjectFactory objectFactory) {
        Object mutex = RequestContextHolder.currentRequestAttributes().getSessionMutex();
        synchronized (mutex) {
            return super.get(name, objectFactory);
        }
    }

    @Override
    public Object remove(String name) {
        Object mutex = RequestContextHolder.currentRequestAttributes().getSessionMutex();
        synchronized (mutex) {
            return super.remove(name);
        }
    }

}
