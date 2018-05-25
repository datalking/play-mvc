package com.github.datalking.common.convert.editor;

import java.beans.PropertyEditorSupport;
import java.util.TimeZone;

/**
 */
public class TimeZoneEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(TimeZone.getTimeZone(text));
	}

	@Override
	public String getAsText() {
		return null;
	}

}
