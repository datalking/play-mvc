package com.github.datalking.common;

/**
 * 获取方法参数的默认实现类
 *
 * @author yaoo on 4/19/18
 */
public class DefaultParameterNameDiscoverer extends PrioritizedParameterNameDiscoverer {

    public DefaultParameterNameDiscoverer() {
        addDiscoverer(new StandardReflectionParameterNameDiscoverer());
    }

}
