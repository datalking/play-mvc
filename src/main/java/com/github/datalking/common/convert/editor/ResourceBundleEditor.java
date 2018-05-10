package com.github.datalking.common.convert.editor;

import com.github.datalking.util.Assert;
import com.github.datalking.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 */
public class ResourceBundleEditor extends PropertyEditorSupport {

    public static final String BASE_NAME_SEPARATOR = "_";


    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        Assert.hasText(text, "'text' must not be empty");
        ResourceBundle bundle;
        String rawBaseName = text.trim();
        int indexOfBaseNameSeparator = rawBaseName.indexOf(BASE_NAME_SEPARATOR);
        if (indexOfBaseNameSeparator == -1) {
            bundle = ResourceBundle.getBundle(rawBaseName);
        } else {
            // it potentially has locale information
            String baseName = rawBaseName.substring(0, indexOfBaseNameSeparator);
            if (!StringUtils.hasText(baseName)) {
                throw new IllegalArgumentException("Bad ResourceBundle name : received '" + text + "' as argument to 'setAsText(String value)'.");
            }
            String localeString = rawBaseName.substring(indexOfBaseNameSeparator + 1);
//			Locale locale = StringUtils.parseLocaleString(localeString);
            Locale locale = Locale.ENGLISH;
            bundle = (StringUtils.hasText(localeString))
                    ? ResourceBundle.getBundle(baseName, locale)
                    : ResourceBundle.getBundle(baseName);
        }
        setValue(bundle);
    }

}
