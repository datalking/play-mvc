package com.github.datalking.aop.interceptor;

import com.github.datalking.aop.Advisor;
import com.github.datalking.aop.support.DefaultPointcutAdvisor;
import com.github.datalking.common.NamedThreadLocal;
import com.github.datalking.common.PriorityOrdered;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;

/**
 * copied from spring
 */
public class ExposeInvocationInterceptor implements MethodInterceptor, PriorityOrdered, Serializable {

    public static final ExposeInvocationInterceptor INSTANCE = new ExposeInvocationInterceptor();

    private static final ThreadLocal<MethodInvocation> invocation = new NamedThreadLocal<>("Current AOP method invocation");

    public static final Advisor ADVISOR = new DefaultPointcutAdvisor(INSTANCE) {
        @Override
        public String toString() {
            return ExposeInvocationInterceptor.class.getName() + ".ADVISOR";
        }
    };

    public static MethodInvocation currentInvocation() throws IllegalStateException {
        MethodInvocation mi = invocation.get();
        if (mi == null)
            throw new IllegalStateException("No MethodInvocation found: Check that an AOP invocation");
        return mi;
    }

    private ExposeInvocationInterceptor() {
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        MethodInvocation oldInvocation = invocation.get();
        invocation.set(mi);
        try {
            return mi.proceed();
        } finally {
            invocation.set(oldInvocation);
        }
    }

    @Override
    public int getOrder() {
        return PriorityOrdered.HIGHEST_PRECEDENCE + 1;
    }

    private Object readResolve() {
        return INSTANCE;
    }


}
