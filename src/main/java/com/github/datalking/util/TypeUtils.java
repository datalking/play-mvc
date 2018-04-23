package com.github.datalking.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * 类型操作工具类
 */
public abstract class TypeUtils {

    /**
     * Check if the right-hand side type may be assigned to the left-hand side
     * type following the Java generics rules.
     *
     * @param lhsType the target type
     * @param rhsType the value type that should be assigned to the target type
     * @return true if rhs is assignable to lhs
     */
    public static boolean isAssignable(Type lhsType, Type rhsType) {
        Assert.notNull(lhsType, "Left-hand side type must not be null");
        Assert.notNull(rhsType, "Right-hand side type must not be null");

        // all types are assignable to themselves and to class Object
        if (lhsType.equals(rhsType) || Object.class == lhsType) {
            return true;
        }

        if (lhsType instanceof Class) {
            Class<?> lhsClass = (Class<?>) lhsType;

            // just comparing two classes
            if (rhsType instanceof Class) {
                return ClassUtils.isAssignable(lhsClass, (Class<?>) rhsType);
            }

            if (rhsType instanceof ParameterizedType) {
                Type rhsRaw = ((ParameterizedType) rhsType).getRawType();

                // a parameterized type is always assignable to its raw class type
                if (rhsRaw instanceof Class) {
                    return ClassUtils.isAssignable(lhsClass, (Class<?>) rhsRaw);
                }
            } else if (lhsClass.isArray() && rhsType instanceof GenericArrayType) {
                Type rhsComponent = ((GenericArrayType) rhsType).getGenericComponentType();

                return isAssignable(lhsClass.getComponentType(), rhsComponent);
            }
        }

        // parameterized types are only assignable to other parameterized types and class types
        if (lhsType instanceof ParameterizedType) {
            if (rhsType instanceof Class) {
                Type lhsRaw = ((ParameterizedType) lhsType).getRawType();

                if (lhsRaw instanceof Class) {
                    return ClassUtils.isAssignable((Class<?>) lhsRaw, (Class<?>) rhsType);
                }
            } else if (rhsType instanceof ParameterizedType) {
                return isAssignable((ParameterizedType) lhsType, (ParameterizedType) rhsType);
            }
        }

        if (lhsType instanceof GenericArrayType) {
            Type lhsComponent = ((GenericArrayType) lhsType).getGenericComponentType();

            if (rhsType instanceof Class) {
                Class<?> rhsClass = (Class<?>) rhsType;

                if (rhsClass.isArray()) {
                    return isAssignable(lhsComponent, rhsClass.getComponentType());
                }
            } else if (rhsType instanceof GenericArrayType) {
                Type rhsComponent = ((GenericArrayType) rhsType).getGenericComponentType();

                return isAssignable(lhsComponent, rhsComponent);
            }
        }

        if (lhsType instanceof WildcardType) {
            return isAssignable((WildcardType) lhsType, rhsType);
        }

        return false;
    }


}
