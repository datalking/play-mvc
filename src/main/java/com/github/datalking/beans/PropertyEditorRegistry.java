package com.github.datalking.beans;

import java.beans.PropertyEditor;

/**
 * @author yaoo on 5/10/18
 */
public interface PropertyEditorRegistry {

    void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);

    void registerCustomEditor(Class<?> requiredType, String propertyPath, PropertyEditor propertyEditor);

    PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath);

}
