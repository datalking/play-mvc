package com.github.datalking.common;

/**
 * 标记顺序的接口
 * <p>
 * 数值越小，优先级越高
 * 大量使用策略设计模式，Ordered接口用来处理相同接口实现类的优先级问题
 *
 * @author yaoo on 4/18/18
 */
public interface Ordered {

    /**
     * 最高优先级，最小Integer
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * 最低优先级，最大Integer
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * 获取优先级
     */
    int getOrder();

}
