package com.github.datalking.web.bind;

import com.github.datalking.beans.MutablePropertyValues;
import com.github.datalking.beans.PropertyValue;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/**
 * @author yaoo on 4/29/18
 */
public class WebDataBinder extends DataBinder {

    public static final String DEFAULT_FIELD_MARKER_PREFIX = "_";

    public static final String DEFAULT_FIELD_DEFAULT_PREFIX = "!";

    private String fieldMarkerPrefix = DEFAULT_FIELD_MARKER_PREFIX;

    private String fieldDefaultPrefix = DEFAULT_FIELD_DEFAULT_PREFIX;

    private boolean bindEmptyMultipartFiles = true;

    public WebDataBinder(Object target) {
        super(target);
    }

    public WebDataBinder(Object target, String objectName) {
        super(target, objectName);
    }

    void setFieldMarkerPrefix(String fieldMarkerPrefix) {
        this.fieldMarkerPrefix = fieldMarkerPrefix;
    }

    public String getFieldMarkerPrefix() {
        return this.fieldMarkerPrefix;
    }

    public void setFieldDefaultPrefix(String fieldDefaultPrefix) {
        this.fieldDefaultPrefix = fieldDefaultPrefix;
    }

    public String getFieldDefaultPrefix() {
        return this.fieldDefaultPrefix;
    }

    public void setBindEmptyMultipartFiles(boolean bindEmptyMultipartFiles) {
        this.bindEmptyMultipartFiles = bindEmptyMultipartFiles;
    }

    public boolean isBindEmptyMultipartFiles() {
        return this.bindEmptyMultipartFiles;
    }

//    @Override
//    protected void doBind(MutablePropertyValues mpvs) {
////        checkFieldDefaults(mpvs);
////        checkFieldMarkers(mpvs);
//        super.doBind(mpvs);
//
//        protected void checkFieldDefaults (MutablePropertyValues mpvs){
//            if (getFieldDefaultPrefix() != null) {
//                String fieldDefaultPrefix = getFieldDefaultPrefix();
//                PropertyValue[] pvArray = mpvs.getPropertyValues();
//                for (PropertyValue pv : pvArray) {
//                    if (pv.getName().startsWith(fieldDefaultPrefix)) {
//                        String field = pv.getName().substring(fieldDefaultPrefix.length());
//                        if (getPropertyAccessor().isWritableProperty(field) && !mpvs.contains(field)) {
//                            mpvs.add(field, pv.getValue());
//                        }
//                        mpvs.removePropertyValue(pv);
//                    }
//                }
//            }
//        }
//
//        protected void checkFieldMarkers (MutablePropertyValues mpvs){
//            if (getFieldMarkerPrefix() != null) {
//                String fieldMarkerPrefix = getFieldMarkerPrefix();
//                PropertyValue[] pvArray = mpvs.getPropertyValues();
//                for (PropertyValue pv : pvArray) {
//                    if (pv.getName().startsWith(fieldMarkerPrefix)) {
//                        String field = pv.getName().substring(fieldMarkerPrefix.length());
//                        if (getPropertyAccessor().isWritableProperty(field) && !mpvs.contains(field)) {
//                            Class fieldType = getPropertyAccessor().getPropertyType(field);
//                            mpvs.add(field, getEmptyValue(field, fieldType));
//                        }
//                        mpvs.removePropertyValue(pv);
//                    }
//                }
//            }
//        }
//
//        protected Object getEmptyValue (String field, Class fieldType){
//            if (fieldType != null && boolean.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
//                // Special handling of boolean property.
//                return Boolean.FALSE;
//            } else if (fieldType != null && fieldType.isArray()) {
//                // Special handling of array property.
//                return Array.newInstance(fieldType.getComponentType(), 0);
//            } else {
//                // Default value: try null.
//                return null;
//            }
//        }
//
//
//        protected void bindMultipart (Map < String, List < MultipartFile >> multipartFiles, MutablePropertyValues mpvs){
//            for (Map.Entry<String, List<MultipartFile>> entry : multipartFiles.entrySet()) {
//                String key = entry.getKey();
//                List<MultipartFile> values = entry.getValue();
//                if (values.size() == 1) {
//                    MultipartFile value = values.get(0);
//                    if (isBindEmptyMultipartFiles() || !value.isEmpty()) {
//                        mpvs.add(key, value);
//                    }
//                } else {
//                    mpvs.add(key, values);
//                }
//            }
//        }
//
//    }

}
