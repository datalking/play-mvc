package com.github.datalking.aop.framework.adapter;

import com.github.datalking.aop.Advisor;
import com.github.datalking.aop.support.DefaultPointcutAdvisor;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装advice为advisor
 *
 * @author yaoo on 4/19/18
 */
public class DefaultAdvisorAdapterRegistry implements AdvisorAdapterRegistry, Serializable {

    private final List<AdvisorAdapter> adapters = new ArrayList<>(3);

    public DefaultAdvisorAdapterRegistry() {
        registerAdvisorAdapter(new MethodBeforeAdviceAdapter());
        registerAdvisorAdapter(new AfterReturningAdviceAdapter());
    }

    @Override
    public void registerAdvisorAdapter(AdvisorAdapter adapter) {
        this.adapters.add(adapter);
    }

    /**
     * 将advice包装成advisor
     */
    @Override
    public Advisor wrap(Object adviceObject) {

        if (adviceObject instanceof Advisor) {
            return (Advisor) adviceObject;
        }

        if (!(adviceObject instanceof Advice)) {
            try {
                throw new Exception(adviceObject + "不是Advice的类型或类型不可识别");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Advice advice = (Advice) adviceObject;

        if (advice instanceof MethodInterceptor) {
            // 不需要适配器
            return new DefaultPointcutAdvisor(advice);
        }


        for (AdvisorAdapter adapter : this.adapters) {
            if (adapter.supportsAdvice(advice)) {
                return new DefaultPointcutAdvisor(advice);
            }
        }

        return null;
    }

    @Override
    public MethodInterceptor[] getInterceptors(Advisor advisor) {
        List<MethodInterceptor> interceptors = new ArrayList<MethodInterceptor>(3);
        Advice advice = advisor.getAdvice();

        if (advice instanceof MethodInterceptor) {
            interceptors.add((MethodInterceptor) advice);
        }

        for (AdvisorAdapter adapter : this.adapters) {
            if (adapter.supportsAdvice(advice)) {
                interceptors.add(adapter.getInterceptor(advisor));
            }
        }

        if (interceptors.isEmpty()) {
            try {
                throw new Exception(advisor.getAdvice() + "可能不是advice类型");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return interceptors.toArray(new MethodInterceptor[interceptors.size()]);
    }

}
