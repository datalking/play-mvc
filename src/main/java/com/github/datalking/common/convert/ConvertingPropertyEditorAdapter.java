package com.github.datalking.common.convert;

import com.github.datalking.common.convert.descriptor.TypeDescriptor;
import com.github.datalking.util.Assert;

import java.beans.PropertyEditorSupport;

/**
 * @author yaoo on 5/10/18
 */
public class ConvertingPropertyEditorAdapter extends PropertyEditorSupport {

    private final ConversionService conversionService;

    private final TypeDescriptor targetDescriptor;

    private final boolean canConvertToString;

    public ConvertingPropertyEditorAdapter(ConversionService conversionService, TypeDescriptor targetDescriptor) {
        Assert.notNull(conversionService, "ConversionService must not be null");
        Assert.notNull(targetDescriptor, "TypeDescriptor must not be null");
        this.conversionService = conversionService;
        this.targetDescriptor = targetDescriptor;
        this.canConvertToString = conversionService.canConvert(this.targetDescriptor, TypeDescriptor.valueOf(String.class));
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(this.conversionService.convert(text, TypeDescriptor.valueOf(String.class), this.targetDescriptor));
    }

    @Override
    public String getAsText() {
        if (this.canConvertToString) {

            return (String) this.conversionService.convert(getValue(), this.targetDescriptor, TypeDescriptor.valueOf(String.class));
        }
        else {
            return null;
        }
    }

}
