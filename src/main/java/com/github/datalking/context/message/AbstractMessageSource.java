package com.github.datalking.context.message;

import com.github.datalking.context.MessageSource;
import com.github.datalking.context.MessageSourceResolvable;
import com.github.datalking.util.ObjectUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * @author yaoo on 5/7/18
 */
public abstract class AbstractMessageSource extends MessageSourceSupport implements MessageSource {

//    private MessageSource parentMessageSource;

    private Properties commonMessages;

    private boolean useCodeAsDefaultMessage = false;


//    public void setParentMessageSource(MessageSource parent) {
//        this.parentMessageSource = parent;
//    }
//
//    public MessageSource getParentMessageSource() {
//        return this.parentMessageSource;
//    }

    public void setCommonMessages(Properties commonMessages) {
        this.commonMessages = commonMessages;
    }

    protected Properties getCommonMessages() {
        return this.commonMessages;
    }

    public void setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
        this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
    }

    protected boolean isUseCodeAsDefaultMessage() {
        return this.useCodeAsDefaultMessage;
    }

    @Override
    public final String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        String msg = getMessageInternal(code, args, locale);
        if (msg != null) {
            return msg;
        }
        if (defaultMessage == null) {
            String fallback = getDefaultMessage(code);
            if (fallback != null) {
                return fallback;
            }
        }
        return renderDefaultMessage(defaultMessage, args, locale);
    }

    @Override
    public final String getMessage(String code, Object[] args, Locale locale) {
        String msg = getMessageInternal(code, args, locale);
        if (msg != null) {
            return msg;
        }
        String fallback = getDefaultMessage(code);
        if (fallback != null) {
            return fallback;
        }

        try {
            throw new Exception(code + locale);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public final String getMessage(MessageSourceResolvable resolvable, Locale locale) {

        String[] codes = resolvable.getCodes();
        if (codes == null) {
            codes = new String[0];
        }
        for (String code : codes) {
            String msg = getMessageInternal(code, resolvable.getArguments(), locale);
            if (msg != null) {
                return msg;
            }
        }
        String defaultMessage = resolvable.getDefaultMessage();
        if (defaultMessage != null) {
            return renderDefaultMessage(defaultMessage, resolvable.getArguments(), locale);
        }
        if (codes.length > 0) {
            String fallback = getDefaultMessage(codes[0]);
            if (fallback != null) {
                return fallback;
            }
        }
        try {
            throw new Exception((codes.length > 0 ? codes[codes.length - 1] : null) + locale);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getMessageInternal(String code, Object[] args, Locale locale) {
        if (code == null) {
            return null;
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        Object[] argsToUse = args;

        if (!isAlwaysUseMessageFormat() && ObjectUtils.isEmpty(args)) {
            // Optimized resolution: no arguments to apply,
            // therefore no MessageFormat needs to be involved.
            // Note that the default implementation still uses MessageFormat;
            // this can be overridden in specific subclasses.
            String message = resolveCodeWithoutArguments(code, locale);
            if (message != null) {
                return message;
            }
        } else {
            // Resolve arguments eagerly, for the case where the message
            // is defined in a parent MessageSource but resolvable arguments
            // are defined in the child MessageSource.
            argsToUse = resolveArguments(args, locale);

            MessageFormat messageFormat = resolveCode(code, locale);
            if (messageFormat != null) {
                synchronized (messageFormat) {
                    return messageFormat.format(argsToUse);
                }
            }
        }

        // Check locale-independent common messages for the given message code.
        Properties commonMessages = getCommonMessages();
        if (commonMessages != null) {
            String commonMessage = commonMessages.getProperty(code);
            if (commonMessage != null) {
                return formatMessage(commonMessage, args, locale);
            }
        }

        // Not found -> check parent, if any.
//        return getMessageFromParent(code, argsToUse, locale);
        return null;
    }

//    protected String getMessageFromParent(String code, Object[] args, Locale locale) {
//        MessageSource parent = getParentMessageSource();
//        if (parent != null) {
//            if (parent instanceof AbstractMessageSource) {
//                // Call internal method to avoid getting the default code back
//                // in case of "useCodeAsDefaultMessage" being activated.
//                return ((AbstractMessageSource) parent).getMessageInternal(code, args, locale);
//            } else {
//                // Check parent MessageSource, returning null if not found there.
//                return parent.getMessage(code, args, null, locale);
//            }
//        }
//        // Not found in parent either.
//        return null;
//    }

    protected String getDefaultMessage(String code) {
        if (isUseCodeAsDefaultMessage()) {
            return code;
        }
        return null;
    }

    @Override
    protected Object[] resolveArguments(Object[] args, Locale locale) {
        if (args == null) {
            return new Object[0];
        }
        List<Object> resolvedArgs = new ArrayList<>(args.length);
        for (Object arg : args) {
            if (arg instanceof MessageSourceResolvable) {
                resolvedArgs.add(getMessage((MessageSourceResolvable) arg, locale));
            } else {
                resolvedArgs.add(arg);
            }
        }
        return resolvedArgs.toArray(new Object[resolvedArgs.size()]);
    }

    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        MessageFormat messageFormat = resolveCode(code, locale);
        if (messageFormat != null) {
            synchronized (messageFormat) {
                return messageFormat.format(new Object[0]);
            }
        }
        return null;
    }

    protected abstract MessageFormat resolveCode(String code, Locale locale);

}
