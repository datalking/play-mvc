package com.github.datalking.common.convert.editor;

import com.github.datalking.util.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 */
public class CharacterEditor extends PropertyEditorSupport {

	private static final String UNICODE_PREFIX = "\\u";

	private static final int UNICODE_LENGTH = 6;

	private final boolean allowEmpty;

	public CharacterEditor(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && !StringUtils.hasLength(text)) {
			setValue(null);
		}
		else if (text == null) {
			throw new IllegalArgumentException("null String cannot be converted to char type");
		}
		else if (isUnicodeCharacterSequence(text)) {
			setAsUnicode(text);
		}
		else if (text.length() != 1) {
			throw new IllegalArgumentException("String [" + text + "] with length " +
					text.length() + " cannot be converted to char type");
		}
		else {
			setValue(new Character(text.charAt(0)));
		}
	}

	@Override
	public String getAsText() {
		Object value = getValue();
		return (value != null ? value.toString() : "");
	}


	private boolean isUnicodeCharacterSequence(String sequence) {
		return (sequence.startsWith(UNICODE_PREFIX) && sequence.length() == UNICODE_LENGTH);
	}

	private void setAsUnicode(String text) {
		int code = Integer.parseInt(text.substring(UNICODE_PREFIX.length()), 16);
		setValue(new Character((char) code));
	}

}
