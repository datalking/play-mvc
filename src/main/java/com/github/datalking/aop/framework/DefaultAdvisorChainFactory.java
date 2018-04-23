package com.github.datalking.aop.framework;

import com.github.datalking.aop.Advisor;
import com.github.datalking.aop.MethodMatcher;
import com.github.datalking.aop.PointcutAdvisor;
import com.github.datalking.aop.framework.adapter.AdvisorAdapterRegistry;
import com.github.datalking.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yaoo on 4/18/18
 */
public class DefaultAdvisorChainFactory implements AdvisorChainFactory, Serializable {


    @Override
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Advised config, Method method, Class<?> targetClass) {

        List<Object> interceptorList = new ArrayList<>(config.getAdvisors().length);

        Class<?> actualClass = (targetClass != null ? targetClass : method.getDeclaringClass());

        // 获取advisor适配器
        AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();

        for (Advisor advisor : config.getAdvisors()) {

            /// 如果是PointcutAdvisor
            if (advisor instanceof PointcutAdvisor) {
                // Add it conditionally.
                PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
                if (pointcutAdvisor.getPointcut().getClassFilter().matches(actualClass)) {
                    MethodInterceptor[] interceptors = registry.getInterceptors(advisor);
                    MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();

                    if (mm.matches(method, actualClass)) {

                        if (mm.isRuntime()) {
                            // 在getInterceptors() 创建新对象
                            for (MethodInterceptor interceptor : interceptors) {
                                interceptorList.add(new InterceptorAndDynamicMethodMatcher(interceptor, mm));
                            }
                        } else {
                            interceptorList.addAll(Arrays.asList(interceptors));
                        }
                    }
                }
            }

            /// 如果不是PointcutAdvisor
            else {
                Interceptor[] interceptors = registry.getInterceptors(advisor);
                interceptorList.addAll(Arrays.asList(interceptors));
            }

        }

        return interceptorList;
    }


}
