package com.github.datalking.aop.framework.adapter;

/**
 * 暴露DefaultAdvisorAdapterRegistry的单例
 *
 * @author yaoo on 4/19/18
 */
public abstract class GlobalAdvisorAdapterRegistry {


    private static AdvisorAdapterRegistry instance = new DefaultAdvisorAdapterRegistry();

    public static AdvisorAdapterRegistry getInstance() {
        return instance;
    }

    static void reset() {
        instance = new DefaultAdvisorAdapterRegistry();
    }

}
