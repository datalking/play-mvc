package com.github.datalking.util;

import com.github.datalking.annotation.Component;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yaoo on 5/6/18
 */
public abstract class AnnoScanUtils {

    /**
     * 判断class上是否有 @Component 注解
     * 对应于spring的ClassPathScanningCandidateComponentProvider.isCandidateComponent()
     *
     * @param clazz 类对象
     * @return 是否有
     */
    public static boolean isCandidateComponent(Class clazz) {

        /// 判断class上直接有@Component
        if (clazz.isAnnotationPresent(Component.class)) {
            return true;
        }

        /// 判断class上的注解的注解包含@Component，如@Controller
        Set<Class> annoAll = getAnnoClassIncludingSuper(clazz);
        if (annoAll != null) {
            for (Class c : annoAll) {
                if (c.getName().equals(Component.class.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 深度优先查找clazz类上注解及注解的注解
     */
    public static Set<Class> getAnnoClassIncludingSuper(Class<?> clazz) {

        Annotation[] allAnnos = clazz.getDeclaredAnnotations();
        if (allAnnos == null) {
            return null;
        }

        // stack存放未访问过的anno
        Deque<Class> stack = new ArrayDeque<>();
        for (Annotation annotation : allAnnos) {
            stack.add(annotation.annotationType());
        }

        // 存放已访问过的anno
        Set<Class> annoAll = new HashSet<>(16);

        while (!stack.isEmpty()) {
            Class c = stack.pop();

            if (!annoAll.contains(c)) {
                annoAll.add(c);

                Annotation[] anno2 = c.getAnnotations();
                for (Annotation a2 : anno2) {
                    Class c2 = a2.annotationType();
                    if (!annoAll.contains(c2) && !stack.contains(c2)) {
                        stack.push(c2);
                    }
                }
            }
        }

        return annoAll;

    }

}
