package com.github.datalking.common.meta;

import com.github.datalking.util.AnnotationUtils;
import com.github.datalking.util.Assert;
import com.github.datalking.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 类的元数据及该类注解元数据
 *
 * @author yaoo on 4/9/18
 */
public class StandardAnnotationMetadata extends StandardClassMetadata implements AnnotationMetadata {

    private final Annotation[] annotations;

    public StandardAnnotationMetadata(Class<?> introspectedClass) {
        super(introspectedClass);
        this.annotations = introspectedClass.getAnnotations();
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public Set<String> getAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Annotation ann : this.annotations) {
            types.add(ann.annotationType().getName());
        }
        return types;
    }

    @Override
    public boolean hasAnnotation(String annotationName) {
        for (Annotation ann : this.annotations) {
            if (ann.annotationType().getName().equals(annotationName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnnotated(String annotationType) {

        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (Annotation ann : anns) {

            if (ann.annotationType().getName().equals(annotationType)) {
                return true;
            }

            for (Annotation metaAnn : ann.annotationType().getAnnotations()) {
                if (metaAnn.annotationType().getName().equals(annotationType)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {

//        Method[] methods = getIntrospectedClass().getDeclaredMethods();
        Method[] methods = getIntrospectedClass().getMethods();
        Set<MethodMetadata> annotatedMethods = new LinkedHashSet<>();

        String basePackage = "";
        Class clazz = null;
        try {
            clazz = Class.forName(basePackage + StringUtils.firstLetterUpperCase(annotationName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (Method method : methods) {

            /// 若是非桥接方法，且方法上存在注解，且方法上有注解annotationName
            if (!method.isBridge() && method.getAnnotations().length > 0 && method.isAnnotationPresent(clazz)) {

                annotatedMethods.add(new StandardMethodMetadata(method));
            }
        }

        return annotatedMethods;
    }

    public Set<MethodMetadata> getAnnotatedMethods(Class<?> clazz) {

//        Method[] methods = getIntrospectedClass().getDeclaredMethods();
        Method[] methods = getIntrospectedClass().getMethods();
        Set<MethodMetadata> annotatedMethods = new LinkedHashSet<>();

        for (Method method : methods) {

            /// 若是 非桥接方法，且方法上存在注解，且方法上有注解annotationName
            if (!method.isBridge() && method.getAnnotations().length > 0 && method.isAnnotationPresent((Class<? extends Annotation>) clazz)) {

                annotatedMethods.add(new StandardMethodMetadata(method));
            }
        }

        return annotatedMethods;
    }


    /**
     * 提取注解中的键值对
     * <p>
     * todo 抽象出通用的提取注解所有键值对的工具
     *
     * @param annotationClass     注解类
     * @param classValuesAsString 值是否转为str，默认false
     * @return 键值对
     */
    public Map<String, Object> getAnnotationAttributes(Class annotationClass, boolean classValuesAsString) {
        Assert.notNull(annotationClass, "输入的注解类不能为空");

        Class<?> clazz = getIntrospectedClass();

        if (!clazz.isAnnotationPresent(annotationClass)) {
            return null;
        }

        // 保存注解的所有属性键值对，属性名 -> 属性值
        Map<String, Object> annoMap = new LinkedHashMap<>();

        Annotation a = clazz.getAnnotation(annotationClass);
        if (a != null) {
            Class<? extends Annotation> type = a.annotationType();
            for (Method method : type.getDeclaredMethods()) {
                Object value = null;
                try {
                    value = method.invoke(a);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                annoMap.put(method.getName(), value);
            }
        }


        // 要提取信息的注解类
//        String annotationName = annotationClass.getName();
//
//        final String componentScanAnnoName = "com.github.datalking.annotation.ComponentScan";
//        /// 若注解为@ComponentScan，则将所有属性值加入map
//        if (annotationName.equals(componentScanAnnoName)) {
//            ComponentScan a = clazz.getAnnotation(ComponentScan.class);
////            ComponentScan a = clazz.getAnnotation(annotationClass);
//            if (a != null) {
//                annoMap.put("basePackages", a.basePackages());
//                annoMap.put("basePackageClasses", a.basePackageClasses());
//                annoMap.put("value", a.value());
//            }
//        }


        return annoMap;
    }

    public Map<String, Object> getAnnotationAttributes(String annotationType, boolean classValuesAsString) {

        Annotation[] anns = getIntrospectedClass().getAnnotations();

        for (Annotation ann : anns) {
            if (ann.annotationType().getName().equals(annotationType)) {
//                return AnnotationUtils.getAnnotationAttributes(ann, classValuesAsString, this.nestedAnnotationsAsMap);
                return AnnotationUtils.getAnnotationAttributes(ann, classValuesAsString, false);
            }
        }
        for (Annotation ann : anns) {
            for (Annotation metaAnn : ann.annotationType().getAnnotations()) {
                if (metaAnn.annotationType().getName().equals(annotationType)) {
                    return AnnotationUtils.getAnnotationAttributes(
                            metaAnn, classValuesAsString, false);
//                            metaAnn, classValuesAsString, this.nestedAnnotationsAsMap);
                }
            }
        }
        return null;
    }

    public Map<String, Object> getAnnotationAttributes(String annotationName) {

        Class clazz = null;
        try {
            clazz = Class.forName(annotationName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return this.getAnnotationAttributes(clazz, false);
    }

    public Set<String> getMetaAnnotationTypes(String annotationType) {

        Annotation[] anns = getIntrospectedClass().getAnnotations();

        for (Annotation ann : anns) {

            if (ann.annotationType().getName().equals(annotationType)) {
                Set<String> types = new LinkedHashSet<>();
                Annotation[] metaAnns = ann.annotationType().getAnnotations();

                for (Annotation metaAnn : metaAnns) {
                    types.add(metaAnn.annotationType().getName());

                    for (Annotation metaMetaAnn : metaAnn.annotationType().getAnnotations()) {
                        types.add(metaMetaAnn.annotationType().getName());
                    }
                }

                return types;
            }
        }
        return null;
    }


}
