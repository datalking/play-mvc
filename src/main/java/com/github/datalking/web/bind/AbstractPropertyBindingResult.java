package com.github.datalking.web.bind;

import com.github.datalking.beans.ConfigurablePropertyAccessor;
import com.github.datalking.beans.PropertyAccessorUtils;
import com.github.datalking.beans.PropertyEditorRegistry;
import com.github.datalking.common.convert.ConversionService;
import com.github.datalking.common.convert.ConvertingPropertyEditorAdapter;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;
import com.github.datalking.util.Assert;
import com.github.datalking.util.BeanUtils;

import java.beans.PropertyEditor;

/**
 * @author yaoo on 5/10/18
 */
public abstract class AbstractPropertyBindingResult extends AbstractBindingResult {

    private ConversionService conversionService;

    protected AbstractPropertyBindingResult(String objectName) {
        super(objectName);
    }

    public void initConversion(ConversionService conversionService) {
        Assert.notNull(conversionService, "ConversionService must not be null");
        this.conversionService = conversionService;
        if (getTarget() != null) {
            getPropertyAccessor().setConversionService(conversionService);
        }
    }

    @Override
    public PropertyEditorRegistry getPropertyEditorRegistry() {
        return getPropertyAccessor();
    }

    @Override
    protected String canonicalFieldName(String field) {
        return PropertyAccessorUtils.canonicalPropertyName(field);
    }

    @Override
    public Class<?> getFieldType(String field) {
        return getPropertyAccessor().getPropertyType(fixedField(field));
    }

    @Override
    protected Object getActualFieldValue(String field) {
        return getPropertyAccessor().getPropertyValue(field);
    }

    @Override
    protected Object formatFieldValue(String field, Object value) {
        String fixedField = fixedField(field);
        // Try custom editor...
        PropertyEditor customEditor = getCustomEditor(fixedField);
        if (customEditor != null) {
            customEditor.setValue(value);
            String textValue = customEditor.getAsText();
            // If the PropertyEditor returned null, there is no appropriate
            // text representation for this value: only use it if non-null.
            if (textValue != null) {
                return textValue;
            }
        }
        if (this.conversionService != null) {
            // Try custom converter...
            TypeDescriptor fieldDesc = getPropertyAccessor().getPropertyTypeDescriptor(fixedField);
            TypeDescriptor strDesc = TypeDescriptor.valueOf(String.class);
            if (fieldDesc != null && this.conversionService.canConvert(fieldDesc, strDesc)) {
                return this.conversionService.convert(value, fieldDesc, strDesc);
            }
        }
        return value;
    }

    protected PropertyEditor getCustomEditor(String fixedField) {
        Class<?> targetType = getPropertyAccessor().getPropertyType(fixedField);
        PropertyEditor editor = getPropertyAccessor().findCustomEditor(targetType, fixedField);
        if (editor == null) {
            editor = BeanUtils.findEditorByConvention(targetType);
        }
        return editor;
    }

    @Override
    public PropertyEditor findEditor(String field, Class<?> valueType) {
        Class<?> valueTypeForLookup = valueType;
        if (valueTypeForLookup == null) {
            valueTypeForLookup = getFieldType(field);
        }
        PropertyEditor editor = super.findEditor(field, valueTypeForLookup);
        if (editor == null && this.conversionService != null) {
            TypeDescriptor td = null;
            if (field != null) {
                TypeDescriptor ptd = getPropertyAccessor().getPropertyTypeDescriptor(fixedField(field));
                if (valueType == null || valueType.isAssignableFrom(ptd.getType())) {
                    td = ptd;
                }
            }
            if (td == null) {
                td = TypeDescriptor.valueOf(valueTypeForLookup);
            }
            if (this.conversionService.canConvert(TypeDescriptor.valueOf(String.class), td)) {
                editor = new ConvertingPropertyEditorAdapter(this.conversionService, td);
            }
        }
        return editor;
    }

    public abstract ConfigurablePropertyAccessor getPropertyAccessor();

}
