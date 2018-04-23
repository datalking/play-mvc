package com.github.datalking.aop.aspectj;

import com.github.datalking.aop.Advisor;
import com.github.datalking.aop.PointcutAdvisor;
import com.github.datalking.aop.aspectj.jadvice.AbstractAspectJAdvice;
import com.github.datalking.aop.framework.AbstractAdvisorAutoProxyCreator;
import com.github.datalking.aop.interceptor.ExposeInvocationInterceptor;

import java.util.List;

/**
 * @author yaoo on 4/18/18
 */
public class AspectJAwareAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator {

    public List<Advisor> sortAdvisors(List<Advisor> advisors) {


        return null;
    }

    @Override
    protected boolean shouldSkip(Class<?> beanClass, String beanName) {

        // 查找所有advisor TODO:缓存名称
        List<Advisor> candidateAdvisors = findCandidateAdvisors();

        for (Advisor advisor : candidateAdvisors) {
            if (advisor instanceof AspectJPointcutAdvisor) {
                if (((AbstractAspectJAdvice) advisor.getAdvice()).getAspectName().equals(beanName)) {
                    return true;
                }
            }
        }

        return super.shouldSkip(beanClass, beanName);
    }

    @Override
    protected void extendAdvisors(List<Advisor> candidateAdvisors) {
        makeAdvisorChainAspectJCapableIfNecessary(candidateAdvisors);
    }

    private boolean makeAdvisorChainAspectJCapableIfNecessary(List<Advisor> advisors) {

        // advisors不为空时才进一步处理
        if (!advisors.isEmpty()) {
            boolean foundAspectJAdvice = false;

            for (Advisor advisor : advisors) {
                if (isAspectJAdvice(advisor)) {
                    foundAspectJAdvice = true;
                }
            }

            if (foundAspectJAdvice && !advisors.contains(ExposeInvocationInterceptor.ADVISOR)) {
                advisors.add(0, ExposeInvocationInterceptor.ADVISOR);
                return true;
            }

        }

        return false;
    }

    private boolean isAspectJAdvice(Advisor advisor) {
        return (advisor instanceof InstantiationModelAwarePointcutAdvisor ||
                advisor.getAdvice() instanceof AbstractAspectJAdvice ||
                (advisor instanceof PointcutAdvisor &&
                        ((PointcutAdvisor) advisor).getPointcut() instanceof AspectJExpressionPointcut));
    }

}
