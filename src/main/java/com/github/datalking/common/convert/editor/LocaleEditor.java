package com.github.datalking.common.convert.editor;

import com.github.datalking.util.StringUtils;

import java.beans.PropertyEditorSupport;

public class LocaleEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
//		setValue(StringUtils.parseLocaleString(text));
        setValue(text);
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        return (value != null ? value.toString() : "");
    }

}
