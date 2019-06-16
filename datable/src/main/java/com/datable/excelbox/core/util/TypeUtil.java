package com.datable.excelbox.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * 类型转换或计算处理 工具类
 *
 * @author jinyaoo
 */
public final class TypeUtil {


    public static Object convertStrToTargetClassObj(String str, Class<?> clazz) {

        if (str == null || str.equals("")) {
            return null;
        }
        if (clazz == Integer.class || clazz == int.class) {
            // todo 科学计数法的问题
            return Integer.parseInt(str);
        }
        if (clazz == Long.class || clazz == long.class) {
            return Long.parseLong(str);
        }
        if (clazz == Float.class || clazz == float.class) {
            return Float.parseFloat(str);
        }
        if (clazz == Double.class || clazz == double.class) {
            return Double.parseDouble(str);
        }
        if (clazz == Character.class || clazz == char.class) {
            return str.toCharArray()[0];
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return Float.parseFloat(str);
        }
        if (clazz == Date.class) {
            // todo 日期类型处理
        }

        return str;
    }

    /**
     * 从方法的泛型参数中获取类型
     *
     * @param list 对象列表
     * @return 列表中对象的类型的Class
     */
    public static Class<?> getGenericTypeFromMethodParameter(final List<?> list) {

        // 获取包含泛型的父类
        Type type = list.getClass().getGenericSuperclass();

        ParameterizedType p = (ParameterizedType) type;

        Class clazz = (Class) p.getActualTypeArguments()[0];

        return clazz;
    }

}
