package com.github.datalking.aop.framework;

import com.github.datalking.common.NamedThreadLocal;

/**
 * @author yaoo on 4/19/18
 */
public class ProxyCreationContext {

    // 保存在advisor匹配期间，被创建代理的bean名称
    private static final ThreadLocal<String> currentProxiedBeanName = new NamedThreadLocal<>("Name of current proxied bean");


    public static String getCurrentProxiedBeanName() {
        return currentProxiedBeanName.get();
    }


    static void setCurrentProxiedBeanName(String beanName) {
        if (beanName != null) {
            currentProxiedBeanName.set(beanName);
        } else {
            currentProxiedBeanName.remove();
        }
    }

}
