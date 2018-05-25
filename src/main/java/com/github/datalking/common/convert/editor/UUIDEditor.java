package com.github.datalking.common.convert.editor;

import com.github.datalking.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.UUID;

/**
 */
public class UUIDEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			setValue(UUID.fromString(text));
		}
		else {
			setValue(null);
		}
	}

	@Override
	public String getAsText() {
		UUID value = (UUID) getValue();
		return (value != null ? value.toString() : "");
	}

}
