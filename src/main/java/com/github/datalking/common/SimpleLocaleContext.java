package com.github.datalking.common;

import com.github.datalking.util.Assert;

import java.util.Locale;

/**
 * @author yaoo on 5/3/18
 */
public class SimpleLocaleContext implements LocaleContext {

    private final Locale locale;

    public SimpleLocaleContext(Locale locale) {
        Assert.notNull(locale, "Locale must not be null");
        this.locale = locale;
    }

    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public String toString() {
        return this.locale.toString();
    }

}
