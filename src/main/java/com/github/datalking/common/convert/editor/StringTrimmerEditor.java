package com.github.datalking.common.convert.editor;

import com.github.datalking.util.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 */
public class StringTrimmerEditor extends PropertyEditorSupport {

	private final String charsToDelete;

	private final boolean emptyAsNull;

	public StringTrimmerEditor(boolean emptyAsNull) {
		this.charsToDelete = null;
		this.emptyAsNull = emptyAsNull;
	}

	public StringTrimmerEditor(String charsToDelete, boolean emptyAsNull) {
		this.charsToDelete = charsToDelete;
		this.emptyAsNull = emptyAsNull;
	}

	@Override
	public void setAsText(String text) {
		if (text == null) {
			setValue(null);
		}
		else {
			String value = text.trim();
			if (this.charsToDelete != null) {
				value = StringUtils.deleteAny(value, this.charsToDelete);
			}
			if (this.emptyAsNull && "".equals(value)) {
				setValue(null);
			}
			else {
				setValue(value);
			}
		}
	}

	@Override
	public String getAsText() {
		Object value = getValue();
		return (value != null ? value.toString() : "");
	}

}
