package com.github.datalking.context.annotation;

import java.lang.annotation.Annotation;

/**
 * 注解和该注解所属的class
 *
 * @author yaoo on 4/17/18
 */
public class AnnoClassTuple2 {

    private Annotation annotation;

    private Class<?> clazz;

    public AnnoClassTuple2() {
    }

    public AnnoClassTuple2(Annotation annotation, Class<?> clazz) {
        this.annotation = annotation;
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnoClassTuple2 that = (AnnoClassTuple2) o;

        if (!annotation.equals(that.annotation)) return false;
        return clazz.getName().equals(that.clazz.getName());
    }

    @Override
    public int hashCode() {
        int result = annotation.hashCode();
        result = 31 * result + clazz.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AnnoClassTuple2{" +
                "annotation=" + annotation +
                ", clazz=" + clazz +
                '}';
    }
}
