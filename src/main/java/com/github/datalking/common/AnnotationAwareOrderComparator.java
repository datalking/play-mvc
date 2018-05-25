package com.github.datalking.common;

import com.github.datalking.annotation.Order;
import com.github.datalking.util.AnnotationUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author yaoo on 5/12/18
 */
public class AnnotationAwareOrderComparator extends OrderComparator {

    public static final AnnotationAwareOrderComparator INSTANCE = new AnnotationAwareOrderComparator();

    @Override
    protected int getOrder(Object obj) {
        if (obj instanceof Ordered) {
            return ((Ordered) obj).getOrder();
        }
        if (obj != null) {
            Class<?> clazz = (obj instanceof Class ? (Class) obj : obj.getClass());
            Order order = AnnotationUtils.findAnnotation(clazz, Order.class);
            if (order != null) {
                return order.value();
            }
        }
        return Ordered.LOWEST_PRECEDENCE;
    }

    public static void sort(List<?> list) {
        if (list.size() > 1) {
            Collections.sort(list, INSTANCE);
        }
    }

    public static void sort(Object[] array) {
        if (array.length > 1) {
            Arrays.sort(array, INSTANCE);
        }
    }

}
