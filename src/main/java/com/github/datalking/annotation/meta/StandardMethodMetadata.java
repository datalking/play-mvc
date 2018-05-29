package com.github.datalking.annotation.meta;

import com.github.datalking.util.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author yaoo on 4/13/18
 */
public class StandardMethodMetadata implements MethodMetadata {


    private final Method introspectedMethod;

    public StandardMethodMetadata(Method introspectedMethod) {
        this.introspectedMethod = introspectedMethod;
    }

    @Override
    public String getMethodName() {
        return this.introspectedMethod.getName();
    }

    @Override
    public String getDeclaringClassName() {
        return this.introspectedMethod.getDeclaringClass().getName();
    }

    @Override
    public String getReturnTypeName() {
        return this.introspectedMethod.getReturnType().getName();
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationType) {

        Annotation[] anns = this.introspectedMethod.getAnnotations();

        for (Annotation ann : anns) {

            if (ann.annotationType().getName().equals(annotationType)) {
                return AnnotationUtils.getAnnotationAttributes(
                        ann, true, false);
//                        ann, true, nestedAnnotationsAsMap);
            }

            for (Annotation metaAnn : ann.annotationType().getAnnotations()) {
                if (metaAnn.annotationType().getName().equals(annotationType)) {
                    return AnnotationUtils.getAnnotationAttributes(
                            metaAnn, true, false);
//                            metaAnn, true, this.nestedAnnotationsAsMap);
                }
            }

        }

        return null;
    }

}
