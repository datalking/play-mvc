package com.github.datalking.web.mvc.method;

import com.github.datalking.annotation.web.ResponseStatus;
import com.github.datalking.common.LocaleContextHolder;
import com.github.datalking.context.MessageSource;
import com.github.datalking.context.MessageSourceAware;
import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.StringUtils;
import com.github.datalking.web.mvc.ModelAndView;
import com.github.datalking.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yaoo on 4/28/18
 */
public class ResponseStatusExceptionResolver extends AbstractHandlerExceptionResolver implements MessageSourceAware {

    private MessageSource messageSource;

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request,
                                              HttpServletResponse response,
                                              Object handler,
                                              Exception ex) {

        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            try {
                return resolveResponseStatus(responseStatus, request, response, handler, ex);
            } catch (Exception resolveEx) {
                logger.warn("Handling of @ResponseStatus resulted in Exception", resolveEx);
            }
        }
        return null;
    }

    protected ModelAndView resolveResponseStatus(ResponseStatus responseStatus,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Object handler,
                                                 Exception ex) throws Exception {

        int statusCode = responseStatus.value().value();
        String reason = responseStatus.reason();
        if (this.messageSource != null) {
            reason = this.messageSource.getMessage(reason, null, reason, LocaleContextHolder.getLocale());
        }
        if (!StringUtils.hasLength(reason)) {
            response.sendError(statusCode);
        } else {
            response.sendError(statusCode, reason);
        }
        return new ModelAndView();
    }

}
