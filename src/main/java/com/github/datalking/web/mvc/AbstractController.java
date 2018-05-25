package com.github.datalking.web.mvc;

import com.github.datalking.util.web.WebUtils;
import com.github.datalking.web.support.WebContentGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author yaoo on 5/4/18
 */
public abstract class AbstractController extends WebContentGenerator implements Controller {

    private boolean synchronizeOnSession = false;

    public final void setSynchronizeOnSession(boolean synchronizeOnSession) {
        this.synchronizeOnSession = synchronizeOnSession;
    }

    public final boolean isSynchronizeOnSession() {
        return this.synchronizeOnSession;
    }


    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

//        checkAndPrepare(request, response, this instanceof LastModified);

        if (this.synchronizeOnSession) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object mutex = WebUtils.getSessionMutex(session);
                synchronized (mutex) {
                    return handleRequestInternal(request, response);
                }
            }
        }

        return handleRequestInternal(request, response);
    }

    protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
