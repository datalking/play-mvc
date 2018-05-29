package com.github.datalking.context.annotation;

import com.github.datalking.annotation.meta.AnnotationAttributes;
import com.github.datalking.annotation.meta.AnnotationMetadata;
import com.github.datalking.annotation.meta.MethodMetadata;

/**
 * @author yaoo on 5/29/18
 */
public class MetadataUtils {

    public static AnnotationAttributes attributesFor(AnnotationMetadata metadata, Class<?> annoClass) {
        return attributesFor(metadata, annoClass.getName());
    }

    public static AnnotationAttributes attributesFor(AnnotationMetadata metadata, String annoClassName) {
        return AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annoClassName, false));
    }

    public static AnnotationAttributes attributesFor(MethodMetadata metadata, Class<?> targetAnno) {
        return AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(targetAnno.getName()));
    }

}
