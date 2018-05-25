package com.github.datalking.context.message;

import com.github.datalking.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author yaoo on 5/7/18
 */
public abstract class MessageSourceSupport {

    private static final MessageFormat INVALID_MESSAGE_FORMAT = new MessageFormat("");

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean alwaysUseMessageFormat = false;
    private final Map<String, Map<Locale, MessageFormat>> messageFormatsPerMessage = new HashMap<>();

    public void setAlwaysUseMessageFormat(boolean alwaysUseMessageFormat) {
        this.alwaysUseMessageFormat = alwaysUseMessageFormat;
    }

    protected boolean isAlwaysUseMessageFormat() {
        return this.alwaysUseMessageFormat;
    }

    protected String renderDefaultMessage(String defaultMessage, Object[] args, Locale locale) {
        return formatMessage(defaultMessage, args, locale);
    }

    protected String formatMessage(String msg, Object[] args, Locale locale) {
        if (msg == null || (!this.alwaysUseMessageFormat && ObjectUtils.isEmpty(args))) {
            return msg;
        }
        MessageFormat messageFormat = null;
        synchronized (this.messageFormatsPerMessage) {
            Map<Locale, MessageFormat> messageFormatsPerLocale = this.messageFormatsPerMessage.get(msg);
            if (messageFormatsPerLocale != null) {
                messageFormat = messageFormatsPerLocale.get(locale);
            } else {
                messageFormatsPerLocale = new HashMap<>();
                this.messageFormatsPerMessage.put(msg, messageFormatsPerLocale);
            }
            if (messageFormat == null) {
                try {
                    messageFormat = createMessageFormat(msg, locale);
                } catch (IllegalArgumentException ex) {
                    // invalid message format - probably not intended for formatting,
                    // rather using a message structure with no arguments involved
                    if (this.alwaysUseMessageFormat) {
                        throw ex;
                    }
                    // silently proceed with raw message if format not enforced
                    messageFormat = INVALID_MESSAGE_FORMAT;
                }
                messageFormatsPerLocale.put(locale, messageFormat);
            }
        }
        if (messageFormat == INVALID_MESSAGE_FORMAT) {
            return msg;
        }
        synchronized (messageFormat) {
            return messageFormat.format(resolveArguments(args, locale));
        }
    }

    protected MessageFormat createMessageFormat(String msg, Locale locale) {
        return new MessageFormat((msg != null ? msg : ""), locale);
    }

    protected Object[] resolveArguments(Object[] args, Locale locale) {
        return args;
    }

}
