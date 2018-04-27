package com.github.datalking.web;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Servlet容器启动时会调用ServletContainerInitializer的onStartup()
 * 容器启动的时候会将@HandlesTypes指定的这个类型下面的子类（实现类，子接口等）传递过来，作为参数传入onStartup()
 * <p>
 * HandlesTypes注解标识SpringServletContainerInitializer 类启动时需要处理的类
 *
 * @author yaoo on 4/25/18
 */
@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {

    /**
     * 容器启动时运行的初始化方法
     *
     * @param webAppInitializerClasses 指定类型及其所有子类型
     * @param servletContext           当前Web应用的ServletContext，一个Web应用对应一个ServletContext
     */
    @Override
    public void onStartup(Set<Class<?>> webAppInitializerClasses, ServletContext servletContext) throws ServletException {

        List<WebApplicationInitializer> initializers = new LinkedList<>();

        if (webAppInitializerClasses != null) {

            for (Class<?> waiClass : webAppInitializerClasses) {

                if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers())
                        && WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
                    try {
                        // ==== 循环实例化指定类
                        initializers.add((WebApplicationInitializer) waiClass.newInstance());
                    } catch (Throwable ex) {
                        throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
                    }
                }
            }
        }

        if (initializers.isEmpty()) {
            servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
            return;
        }

        servletContext.log(initializers.size() + " Spring WebApplicationInitializers detected on classpath");
        //AnnotationAwareOrderComparator.sort(initializers);
        for (WebApplicationInitializer initializer : initializers) {
            // ==== 循环调用onStartup()
            initializer.onStartup(servletContext);
        }
    }


}
