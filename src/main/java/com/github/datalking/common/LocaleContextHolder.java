package com.github.datalking.common;

import java.util.Locale;

/**
 * @author yaoo on 5/3/18
 */
public abstract class LocaleContextHolder {

    private static final ThreadLocal<LocaleContext> localeContextHolder = new NamedThreadLocal<>("Locale context");

    private static final ThreadLocal<LocaleContext> inheritableLocaleContextHolder = new NamedInheritableThreadLocal<>("Locale context");

    public static void resetLocaleContext() {
        localeContextHolder.remove();
        inheritableLocaleContextHolder.remove();
    }

    public static void setLocaleContext(LocaleContext localeContext) {
        setLocaleContext(localeContext, false);
    }

    public static void setLocaleContext(LocaleContext localeContext, boolean inheritable) {
        if (localeContext == null) {
            resetLocaleContext();
        } else {
            if (inheritable) {
                inheritableLocaleContextHolder.set(localeContext);
                localeContextHolder.remove();
            } else {
                localeContextHolder.set(localeContext);
                inheritableLocaleContextHolder.remove();
            }
        }
    }

    public static LocaleContext getLocaleContext() {
        LocaleContext localeContext = localeContextHolder.get();
        if (localeContext == null) {
            localeContext = inheritableLocaleContextHolder.get();
        }
        return localeContext;
    }

    public static void setLocale(Locale locale) {
        setLocale(locale, false);
    }

    public static void setLocale(Locale locale, boolean inheritable) {
        LocaleContext localeContext = (locale != null ? new SimpleLocaleContext(locale) : null);
        setLocaleContext(localeContext, inheritable);
    }

    public static Locale getLocale() {
        LocaleContext localeContext = getLocaleContext();
        return (localeContext != null ? localeContext.getLocale() : Locale.getDefault());
    }

}
