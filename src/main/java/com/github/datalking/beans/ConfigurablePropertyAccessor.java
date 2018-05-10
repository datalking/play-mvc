package com.github.datalking.beans;

import com.github.datalking.common.convert.ConversionService;

/**
 * @author yaoo on 5/10/18
 */
public interface ConfigurablePropertyAccessor extends PropertyAccessor, PropertyEditorRegistry, TypeConverter {

    void setConversionService(ConversionService conversionService);

    ConversionService getConversionService();

    void setExtractOldValueForEditor(boolean extractOldValueForEditor);

    boolean isExtractOldValueForEditor();

}
