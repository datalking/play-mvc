package com.github.datalking.context.message;

import com.github.datalking.context.MessageSource;
import com.github.datalking.context.MessageSourceResolvable;

import java.util.Locale;

/**
 * @author yaoo on 5/7/18
 */
public class DelegatingMessageSource extends MessageSourceSupport implements MessageSource {

    private MessageSource parentMessageSource;

    public void setParentMessageSource(MessageSource parent) {
        this.parentMessageSource = parent;
    }

    public MessageSource getParentMessageSource() {
        return this.parentMessageSource;
    }

    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        if (this.parentMessageSource != null) {
            return this.parentMessageSource.getMessage(code, args, defaultMessage, locale);
        } else {
            return renderDefaultMessage(defaultMessage, args, locale);
        }
    }

    public String getMessage(String code, Object[] args, Locale locale) {
        if (this.parentMessageSource != null) {
            return this.parentMessageSource.getMessage(code, args, locale);
        } else {
            try {
                throw new Exception(code + locale);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    public String getMessage(MessageSourceResolvable resolvable, Locale locale) {
        if (this.parentMessageSource != null) {
            return this.parentMessageSource.getMessage(resolvable, locale);
        }
        else {
            if (resolvable.getDefaultMessage() != null) {
                return renderDefaultMessage(resolvable.getDefaultMessage(), resolvable.getArguments(), locale);
            }
            String[] codes = resolvable.getCodes();
            String code = (codes != null && codes.length > 0 ? codes[0] : null);
            try {
                throw new Exception(code+locale);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
