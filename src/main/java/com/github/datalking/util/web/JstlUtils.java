package com.github.datalking.util.web;

import com.github.datalking.context.MessageSource;
import com.github.datalking.context.message.MessageSourceResourceBundle;
import com.github.datalking.context.message.ResourceBundleMessageSource;
import com.github.datalking.web.support.RequestContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * jstl工具类
 */
public abstract class JstlUtils {

    public static MessageSource getJstlAwareMessageSource(
            ServletContext servletContext, MessageSource messageSource) {
        if (servletContext != null) {
            String jstlInitParam = servletContext.getInitParameter(Config.FMT_LOCALIZATION_CONTEXT);
            if (jstlInitParam != null) {
                // Create a ResourceBundleMessageSource for the specified resource bundle
                // basename in the JSTL context-param in web.xml, wiring it with the given
                // Spring-defined MessageSource as parent.
                    ResourceBundleMessageSource jstlBundleWrapper = new ResourceBundleMessageSource();
                jstlBundleWrapper.setBasename(jstlInitParam);
//                jstlBundleWrapper.setParentMessageSource(messageSource);
                return jstlBundleWrapper;
            }
        }
        return messageSource;
    }

    public static void exposeLocalizationContext(HttpServletRequest request, MessageSource messageSource) {
//        Locale jstlLocale = RequestContextUtils.getLocale(request);
//        Config.set(request, Config.FMT_LOCALE, jstlLocale);
        if (messageSource != null) {
            LocalizationContext jstlContext = new SpringLocalizationContext(messageSource, request);
            Config.set(request, Config.FMT_LOCALIZATION_CONTEXT, jstlContext);
        }
    }

    public static void exposeLocalizationContext(RequestContext requestContext) {
        Config.set(requestContext.getRequest(), Config.FMT_LOCALE, requestContext.getLocale());
        MessageSource messageSource = getJstlAwareMessageSource(requestContext.getServletContext(), requestContext.getMessageSource());
        LocalizationContext jstlContext = new SpringLocalizationContext(messageSource, requestContext.getRequest());
        Config.set(requestContext.getRequest(), Config.FMT_LOCALIZATION_CONTEXT, jstlContext);
    }

    private static class SpringLocalizationContext extends LocalizationContext {

        private final MessageSource messageSource;

        private final HttpServletRequest request;

        public SpringLocalizationContext(MessageSource messageSource, HttpServletRequest request) {
            this.messageSource = messageSource;
            this.request = request;
        }

        @Override
        public ResourceBundle getResourceBundle() {
            HttpSession session = this.request.getSession(false);
            if (session != null) {
                Object lcObject = Config.get(session, Config.FMT_LOCALIZATION_CONTEXT);
                if (lcObject instanceof LocalizationContext) {
                    ResourceBundle lcBundle = ((LocalizationContext) lcObject).getResourceBundle();
                    return new MessageSourceResourceBundle(this.messageSource, getLocale(), lcBundle);
                }
            }
            return new MessageSourceResourceBundle(this.messageSource, getLocale());
        }

        @Override
        public Locale getLocale() {
            HttpSession session = this.request.getSession(false);
            if (session != null) {
                Object localeObject = Config.get(session, Config.FMT_LOCALE);
                if (localeObject instanceof Locale) {
                    return (Locale) localeObject;
                }
            }
//            return RequestContextUtils.getLocale(this.request);
            return Locale.ENGLISH;
        }
    }

}
