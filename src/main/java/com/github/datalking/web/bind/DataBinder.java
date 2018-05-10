package com.github.datalking.web.bind;

import com.github.datalking.beans.MutablePropertyValues;
import com.github.datalking.beans.PropertyEditorRegistry;
import com.github.datalking.beans.PropertyValues;
import com.github.datalking.beans.TypeConverter;
import com.github.datalking.common.MethodParameter;
import com.github.datalking.common.convert.ConversionService;
import com.github.datalking.common.convert.SimpleTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设置target对象的属性
 *
 * @author yaoo on 4/29/18
 */
public class DataBinder implements PropertyEditorRegistry, TypeConverter {

    public static final String DEFAULT_OBJECT_NAME = "target";

    public static final int DEFAULT_AUTO_GROW_COLLECTION_LIMIT = 256;

    protected static final Logger logger = LoggerFactory.getLogger(DataBinder.class);

    private final Object target;

    private final String objectName;

    private SimpleTypeConverter typeConverter;

//    private AbstractPropertyBindingResult bindingResult;

    private ConversionService conversionService;

    public DataBinder(Object target) {
        this(target, DEFAULT_OBJECT_NAME);
    }

    public DataBinder(Object target, String objectName) {
        this.target = target;
        this.objectName = objectName;
    }

    public Object getTarget() {
        return this.target;
    }

    public String getObjectName() {
        return this.objectName;
    }


    public void bind(PropertyValues pvs) {
        MutablePropertyValues mpvs = (pvs instanceof MutablePropertyValues) ? (MutablePropertyValues) pvs : new MutablePropertyValues(pvs);
        doBind(mpvs);
    }

    protected void doBind(MutablePropertyValues mpvs) {
//        checkAllowedFields(mpvs);
//        checkRequiredFields(mpvs);
        applyPropertyValues(mpvs);
    }

    protected void applyPropertyValues(MutablePropertyValues mpvs) {

    }

    protected PropertyEditorRegistry getPropertyEditorRegistry() {
//        if (getTarget() != null) {
//            return getInternalBindingResult().getPropertyAccessor();
//        } else {
            return getSimpleTypeConverter();
//        }
    }

    protected SimpleTypeConverter getSimpleTypeConverter() {
        if (this.typeConverter == null) {
            this.typeConverter = new SimpleTypeConverter();
            if (this.conversionService != null) {
                this.typeConverter.setConversionService(this.conversionService);
            }
        }
        return this.typeConverter;
    }

//    protected AbstractPropertyBindingResult getInternalBindingResult() {
//        if (this.bindingResult == null) {
//            initBeanPropertyAccess();
//        }
//        return this.bindingResult;
//    }

    // ======== PropertyEditorRegistry Interface ========


    // ======== TypeConverter Interface ========

    public <T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParam) {

        return getTypeConverter().convertIfNecessary(value, requiredType, methodParam);
    }


}
