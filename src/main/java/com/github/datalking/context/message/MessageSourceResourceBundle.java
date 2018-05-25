package com.github.datalking.context.message;

import com.github.datalking.context.MessageSource;
import com.github.datalking.util.Assert;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author yaoo on 5/7/18
 */
public class MessageSourceResourceBundle extends ResourceBundle {

    private final MessageSource messageSource;

    private final Locale locale;

    public MessageSourceResourceBundle(MessageSource source, Locale locale) {
        Assert.notNull(source, "MessageSource must not be null");
        this.messageSource = source;
        this.locale = locale;
    }

    public MessageSourceResourceBundle(MessageSource source, Locale locale, ResourceBundle parent) {
        this(source, locale);
        setParent(parent);
    }

    @Override
    protected Object handleGetObject(String key) {
        try {
            return this.messageSource.getMessage(key, null, this.locale);
        }
        catch (Exception ex) {
            return null;
        }
    }

    @Override
    public boolean containsKey(String key) {
        try {
            this.messageSource.getMessage(key, null, this.locale);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    @Override
    public Enumeration<String> getKeys() {
        throw new UnsupportedOperationException("MessageSourceResourceBundle does not support enumerating its keys");
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

}
