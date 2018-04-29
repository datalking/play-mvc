package com.github.datalking.common;

import com.github.datalking.exception.Errors;

import java.beans.PropertyEditor;
import java.util.Map;

/**
 * 参数绑定的结果 接口
 *
 * @author yaoo on 4/29/18
 */
public interface BindingResult extends Errors {

    String MODEL_KEY_PREFIX = BindingResult.class.getName() + ".";

    Object getTarget();

    Map<String, Object> getModel();

    Object getRawFieldValue(String field);

    PropertyEditor findEditor(String field, Class<?> valueType);

//    PropertyEditorRegistry getPropertyEditorRegistry();

//    void addError(ObjectError error);

    String[] resolveMessageCodes(String errorCode);

    String[] resolveMessageCodes(String errorCode, String field);

    void recordSuppressedField(String field);

    String[] getSuppressedFields();

}
