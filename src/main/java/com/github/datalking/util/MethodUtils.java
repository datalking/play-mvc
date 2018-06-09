package com.github.datalking.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author yaoo on 6/9/18
 */
public abstract class MethodUtils {

    /**
     * 获取所有public方法，包括父类中的
     * todo 处理重载时方法返回值范围变小的
     */
    public static Set<Method> getMethodsIncludingParent(Class<?> clazz) {
        Assert.notNull(clazz, "input class args cannot be null.");
        Set<Method> methodList = new HashSet<>();

        do {
            List<Method> list = Arrays.asList(clazz.getMethods());
            methodList.addAll(list);
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class);

        return methodList;
    }

    public static Set<Method> getSetMethodsIncludingParent(Class<?> clazz) {
        Set<Method> methodList = getMethodsIncludingParent(clazz);

        /// 遍历所有方法，删除非set方法
        for (Iterator<Method> it = methodList.iterator(); it.hasNext(); ) {
            String mName = it.next().getName();
            if (!mName.startsWith("set")) {
                it.remove();
            }
        }

        return methodList;
    }

}
