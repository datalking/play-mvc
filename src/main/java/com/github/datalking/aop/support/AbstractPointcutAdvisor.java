package com.github.datalking.aop.support;

import com.github.datalking.aop.PointcutAdvisor;
import com.github.datalking.common.Ordered;
import org.aopalliance.aop.Advice;

import java.io.Serializable;

/**
 * 保存advice
 *
 * @author yaoo on 4/18/18
 */
public abstract class AbstractPointcutAdvisor implements PointcutAdvisor, Ordered, Serializable {

    private Advice advice;

    private int order;

    @Override
    public Advice getAdvice() {
        return advice;
    }

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }


    @Override
    public String toString() {
        return "AbsPctAdvisor{" +
                "advice=" + advice +
                ", order=" + order +
                '}';
    }
}
