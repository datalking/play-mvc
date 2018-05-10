package com.github.datalking.common;

import com.github.datalking.common.format.MessageCodeFormatter;
import com.github.datalking.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yaoo on 5/10/18
 */
public class DefaultMessageCodesResolver implements MessageCodesResolver, Serializable {

    public static final String CODE_SEPARATOR = ".";

    private static final MessageCodeFormatter DEFAULT_FORMATTER = Format.PREFIX_ERROR_CODE;

    private String prefix = "";

    private MessageCodeFormatter formatter = DEFAULT_FORMATTER;

    public void setPrefix(String prefix) {
        this.prefix = (prefix != null ? prefix : "");
    }

    public void setMessageCodeFormatter(MessageCodeFormatter formatter) {
        this.formatter = (formatter == null ? DEFAULT_FORMATTER : formatter);
    }

    protected String getPrefix() {
        return this.prefix;
    }

    public String[] resolveMessageCodes(String errorCode, String objectName) {
        return resolveMessageCodes(errorCode, objectName, "", null);
    }

    public String[] resolveMessageCodes(String errorCode, String objectName, String field, Class<?> fieldType) {

        Set<String> codeList = new LinkedHashSet<>();
        List<String> fieldList = new ArrayList<>();

        buildFieldList(field, fieldList);
        addCodes(codeList, errorCode, objectName, fieldList);

        int dotIndex = field.lastIndexOf('.');
        if (dotIndex != -1) {
            buildFieldList(field.substring(dotIndex + 1), fieldList);
        }

        addCodes(codeList, errorCode, null, fieldList);

        if (fieldType != null) {
            addCode(codeList, errorCode, null, fieldType.getName());
        }

        addCode(codeList, errorCode, null, null);

        return StringUtils.toStringArray(codeList);
    }

    private void addCodes(Collection<String> codeList, String errorCode, String objectName, Iterable<String> fields) {
        for (String field : fields) {
            addCode(codeList, errorCode, objectName, field);
        }
    }

    private void addCode(Collection<String> codeList, String errorCode, String objectName, String field) {
        codeList.add(postProcessMessageCode(this.formatter.format(errorCode, objectName, field)));
    }

    protected void buildFieldList(String field, List<String> fieldList) {
        fieldList.add(field);
        String plainField = field;
        int keyIndex = plainField.lastIndexOf('[');
        while (keyIndex != -1) {
            int endKeyIndex = plainField.indexOf(']', keyIndex);
            if (endKeyIndex != -1) {
                plainField = plainField.substring(0, keyIndex) + plainField.substring(endKeyIndex + 1);
                fieldList.add(plainField);
                keyIndex = plainField.lastIndexOf('[');
            }
            else {
                keyIndex = -1;
            }
        }
    }

    protected String postProcessMessageCode(String code) {
        return getPrefix() + code;
    }

    public enum Format implements MessageCodeFormatter {

        PREFIX_ERROR_CODE {
            public String format(String errorCode, String objectName, String field) {
                return toDelimitedString(errorCode, objectName, field);
            }
        },

        POSTFIX_ERROR_CODE {
            public String format(String errorCode, String objectName, String field) {
                return toDelimitedString(objectName, field, errorCode);
            }
        };

        public static String toDelimitedString(String... elements) {
            StringBuilder rtn = new StringBuilder();
            for (String element : elements) {
                if (StringUtils.hasLength(element)) {
                    rtn.append(rtn.length() == 0 ? "" : CODE_SEPARATOR);
                    rtn.append(element);
                }
            }
            return rtn.toString();
        }
    }

}
