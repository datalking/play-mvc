package com.github.datalking.common.convert;

import com.github.datalking.beans.PropertyAccessor;
import com.github.datalking.beans.PropertyEditorRegistry;
import com.github.datalking.common.convert.editor.ByteArrayPropertyEditor;
import com.github.datalking.common.convert.editor.CharArrayPropertyEditor;
import com.github.datalking.common.convert.editor.CharacterEditor;
import com.github.datalking.common.convert.editor.CharsetEditor;
import com.github.datalking.common.convert.editor.ClassArrayEditor;
import com.github.datalking.common.convert.editor.ClassEditor;
import com.github.datalking.common.convert.editor.CurrencyEditor;
import com.github.datalking.common.convert.editor.CustomBooleanEditor;
import com.github.datalking.common.convert.editor.CustomCollectionEditor;
import com.github.datalking.common.convert.editor.CustomMapEditor;
import com.github.datalking.common.convert.editor.CustomNumberEditor;
import com.github.datalking.common.convert.editor.FileEditor;
import com.github.datalking.common.convert.editor.InputSourceEditor;
import com.github.datalking.common.convert.editor.InputStreamEditor;
import com.github.datalking.common.convert.editor.LocaleEditor;
import com.github.datalking.common.convert.editor.PatternEditor;
import com.github.datalking.common.convert.editor.PropertiesEditor;
import com.github.datalking.common.convert.editor.TimeZoneEditor;
import com.github.datalking.common.convert.editor.URIEditor;
import com.github.datalking.common.convert.editor.URLEditor;
import com.github.datalking.common.convert.editor.UUIDEditor;
import com.github.datalking.util.ClassUtils;
import org.xml.sax.InputSource;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 注册默认与自定义属性编辑器
 *
 * @author yaoo on 5/10/18
 */
public class PropertyEditorRegistrySupport implements PropertyEditorRegistry {

    private ConversionService conversionService;

    private boolean defaultEditorsActive = false;
//    private boolean configValueEditorsActive = false;

    private Map<Class<?>, PropertyEditor> defaultEditors;

    private Map<Class<?>, PropertyEditor> customEditors;

    private Map<Class<?>, PropertyEditor> customEditorCache;

//    private Map<Class<?>, PropertyEditor> overriddenDefaultEditors;
//    private Map<String, CustomEditorHolder> customEditorsForPath;
// 支持并发的属性编辑器
//    private Set<PropertyEditor> sharedEditors;

    public ConversionService getConversionService() {
        return this.conversionService;
    }

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    protected void registerDefaultEditors() {
        this.defaultEditorsActive = true;
    }

//    public void useConfigValueEditors() {
//        this.configValueEditorsActive = true;
//    }
//    public void overrideDefaultEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
//        if (this.overriddenDefaultEditors == null) {
//            this.overriddenDefaultEditors = new HashMap<Class<?>, PropertyEditor>();
//        }
//        this.overriddenDefaultEditors.put(requiredType, propertyEditor);
//    }

    public PropertyEditor getDefaultEditor(Class<?> requiredType) {
        if (!this.defaultEditorsActive) {
            return null;
        }
//        if (this.overriddenDefaultEditors != null) {
//            PropertyEditor editor = this.overriddenDefaultEditors.get(requiredType);
//            if (editor != null) {
//                return editor;
//            }
//        }
        if (this.defaultEditors == null) {
            createDefaultEditors();
        }
        return this.defaultEditors.get(requiredType);
    }

    private void createDefaultEditors() {
        this.defaultEditors = new HashMap<>(64);

        // 基本类型 属性编辑器
        this.defaultEditors.put(Charset.class, new CharsetEditor());
        this.defaultEditors.put(Class.class, new ClassEditor());
        this.defaultEditors.put(Class[].class, new ClassArrayEditor());
        this.defaultEditors.put(Currency.class, new CurrencyEditor());
        this.defaultEditors.put(File.class, new FileEditor());
        this.defaultEditors.put(InputStream.class, new InputStreamEditor());
        this.defaultEditors.put(InputSource.class, new InputSourceEditor());
        this.defaultEditors.put(Locale.class, new LocaleEditor());
        this.defaultEditors.put(Pattern.class, new PatternEditor());
        this.defaultEditors.put(Properties.class, new PropertiesEditor());
//        this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
        this.defaultEditors.put(TimeZone.class, new TimeZoneEditor());
        this.defaultEditors.put(URI.class, new URIEditor());
        this.defaultEditors.put(URL.class, new URLEditor());
        this.defaultEditors.put(UUID.class, new UUIDEditor());

        // 集合类型 属性编辑器
        this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
        this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
        this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
        this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
        this.defaultEditors.put(SortedMap.class, new CustomMapEditor(SortedMap.class));

        // primitive arrays
        this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
        this.defaultEditors.put(char[].class, new CharArrayPropertyEditor());

        this.defaultEditors.put(char.class, new CharacterEditor(false));
        this.defaultEditors.put(Character.class, new CharacterEditor(true));

        this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));
        this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));

        // 数值类型
        this.defaultEditors.put(byte.class, new CustomNumberEditor(Byte.class, false));
        this.defaultEditors.put(Byte.class, new CustomNumberEditor(Byte.class, true));
        this.defaultEditors.put(short.class, new CustomNumberEditor(Short.class, false));
        this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class, true));
        this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
        this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
        this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
        this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
        this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
        this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
        this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
        this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
        this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
        this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));

//        if (this.configValueEditorsActive) {
//            StringArrayPropertyEditor sae = new StringArrayPropertyEditor();
//            this.defaultEditors.put(String[].class, sae);
//            this.defaultEditors.put(short[].class, sae);
//            this.defaultEditors.put(int[].class, sae);
//            this.defaultEditors.put(long[].class, sae);
//        }
    }

    protected void copyDefaultEditorsTo(PropertyEditorRegistrySupport target) {
        target.defaultEditorsActive = this.defaultEditorsActive;
//        target.configValueEditorsActive = this.configValueEditorsActive;
        target.defaultEditors = this.defaultEditors;
//        target.overriddenDefaultEditors = this.overriddenDefaultEditors;
    }

    @Override
    public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
        registerCustomEditor(requiredType, null, propertyEditor);
    }

    @Override
    public void registerCustomEditor(Class<?> requiredType, String propertyPath, PropertyEditor propertyEditor) {
        if (requiredType == null && propertyPath == null) {
            throw new IllegalArgumentException("Either requiredType or propertyPath is required");
        }
        if (propertyPath != null) {
//            if (this.customEditorsForPath == null) {
//                this.customEditorsForPath = new LinkedHashMap<String, CustomEditorHolder>(16);
//            }
//            this.customEditorsForPath.put(propertyPath, new CustomEditorHolder(propertyEditor, requiredType));
        } else {
            if (this.customEditors == null) {
                this.customEditors = new LinkedHashMap<>(16);
            }
            this.customEditors.put(requiredType, propertyEditor);
            this.customEditorCache = null;
        }
    }

    @Override
    public PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath) {
        Class<?> requiredTypeToUse = requiredType;
        if (propertyPath != null) {
//            if (this.customEditorsForPath != null) {
//                // Check property-specific editor first.
//                PropertyEditor editor = getCustomEditor(propertyPath, requiredType);
//                if (editor == null) {
//                    List<String> strippedPaths = new LinkedList<String>();
//                    addStrippedPropertyPaths(strippedPaths, "", propertyPath);
//                    for (Iterator<String> it = strippedPaths.iterator(); it.hasNext() && editor == null;) {
//                        String strippedPath = it.next();
//                        editor = getCustomEditor(strippedPath, requiredType);
//                    }
//                }
//                if (editor != null) {
//                    return editor;
//                }
//            }
            if (requiredType == null) {
                requiredTypeToUse = getPropertyType(propertyPath);
            }
        }
        return getCustomEditor(requiredTypeToUse);
    }

    protected Class<?> getPropertyType(String propertyPath) {
        return null;
    }

    private PropertyEditor getCustomEditor(String propertyName, Class<?> requiredType) {
//        CustomEditorHolder holder = this.customEditorsForPath.get(propertyName);
//        return (holder != null ? holder.getPropertyEditor(requiredType) : null);
        return null;
    }

    private PropertyEditor getCustomEditor(Class<?> requiredType) {
        if (requiredType == null || this.customEditors == null) {
            return null;
        }
        PropertyEditor editor = this.customEditors.get(requiredType);
        if (editor == null) {
//            if (this.customEditorCache != null) {
//                editor = this.customEditorCache.get(requiredType);
//            }
            if (editor == null) {
                // Find editor for superclass or interface.
                for (Iterator<Class<?>> it = this.customEditors.keySet().iterator(); it.hasNext() && editor == null; ) {
                    Class<?> key = it.next();
                    if (key.isAssignableFrom(requiredType)) {
                        editor = this.customEditors.get(key);
                        // Cache editor for search type, to avoid the overhead of repeated assignable-from checks.
                        if (this.customEditorCache == null) {
                            this.customEditorCache = new HashMap<>();
                        }
                        this.customEditorCache.put(requiredType, editor);
                    }
                }
            }
        }
        return editor;
    }

    protected Class<?> guessPropertyTypeFromEditors(String propertyName) {
//        if (this.customEditorsForPath != null) {
//            CustomEditorHolder editorHolder = this.customEditorsForPath.get(propertyName);
//            if (editorHolder == null) {
//                List<String> strippedPaths = new LinkedList<String>();
//                addStrippedPropertyPaths(strippedPaths, "", propertyName);
//                for (Iterator<String> it = strippedPaths.iterator(); it.hasNext() && editorHolder == null; ) {
//                    String strippedName = it.next();
//                    editorHolder = this.customEditorsForPath.get(strippedName);
//                }
//            }
//            if (editorHolder != null) {
//                return editorHolder.getRegisteredType();
//            }
//        }
        return null;
    }


    private void addStrippedPropertyPaths(List<String> strippedPaths, String nestedPath, String propertyPath) {
        int startIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
        if (startIndex != -1) {
            int endIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR);
            if (endIndex != -1) {
                String prefix = propertyPath.substring(0, startIndex);
                String key = propertyPath.substring(startIndex, endIndex + 1);
                String suffix = propertyPath.substring(endIndex + 1, propertyPath.length());
                // Strip the first key.
                strippedPaths.add(nestedPath + prefix + suffix);
                // Search for further keys to strip, with the first key stripped.
                addStrippedPropertyPaths(strippedPaths, nestedPath + prefix, suffix);
                // Search for further keys to strip, with the first key not stripped.
                addStrippedPropertyPaths(strippedPaths, nestedPath + prefix + key, suffix);
            }
        }
    }


    /**
     * 已注册的属性编辑器及其对应的class
     */
    private static class CustomEditorHolder {

        private final PropertyEditor propertyEditor;

        private final Class<?> registeredType;

        private CustomEditorHolder(PropertyEditor propertyEditor, Class<?> registeredType) {
            this.propertyEditor = propertyEditor;
            this.registeredType = registeredType;
        }

        private PropertyEditor getPropertyEditor() {
            return this.propertyEditor;
        }

        private Class<?> getRegisteredType() {
            return this.registeredType;
        }

        private PropertyEditor getPropertyEditor(Class<?> requiredType) {

            if (this.registeredType == null ||
                    (requiredType != null &&
                            (ClassUtils.isAssignable(this.registeredType, requiredType) ||
                                    ClassUtils.isAssignable(requiredType, this.registeredType))) ||
                    (requiredType == null && (!Collection.class.isAssignableFrom(this.registeredType) && !this.registeredType.isArray()))) {
                return this.propertyEditor;
            } else {
                return null;
            }
        }
    }

}
