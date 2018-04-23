package com.github.datalking.aop.framework;

import com.github.datalking.aop.Advisor;
import com.github.datalking.aop.EmptyTargetSource;
import com.github.datalking.aop.SingletonTargetSource;
import com.github.datalking.aop.TargetSource;
import com.github.datalking.aop.support.DefaultPointcutAdvisor;
import com.github.datalking.util.Assert;
import org.aopalliance.aop.Advice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * proxy配置管理器
 *
 * @author yaoo on 4/18/18
 */
public class AdvisedSupport extends ProxyConfig implements Advised {

    public static final TargetSource EMPTY_TARGET_SOURCE = EmptyTargetSource.INSTANCE;

    TargetSource targetSource = EMPTY_TARGET_SOURCE;

    // 代理要实现的接口
    private List<Class<?>> interfaces = new ArrayList<>();

    // 添加的advice会包装成advisor，保存在这里
    private List<Advisor> advisors = new LinkedList<>();

    // 方便内部操作
    private Advisor[] advisorArray = new Advisor[0];

    AdvisorChainFactory advisorChainFactory = new DefaultAdvisorChainFactory();

    // 缓存方法和advisor链
    private transient Map<MethodCacheKey, List<Object>> methodCache;

    public AdvisedSupport() {
        //initMethodCache();
        this.methodCache = new ConcurrentHashMap<>(32);

    }

    public AdvisedSupport(Class<?>... interfaces) {
        this();
        setInterfaces(interfaces);
    }

//    private void initMethodCache() {
//        this.methodCache = new ConcurrentHashMap<>(32);
//    }

    public void setInterfaces(Class<?>... interfaces) {
        Assert.notNull(interfaces, "Interfaces must not be null");
        this.interfaces.clear();
        for (Class<?> c : interfaces) {
            addInterface(c);
        }
    }

    public void addInterface(Class<?> interfaceClass) {
        Assert.notNull(interfaceClass, "Interface must not be null");
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("[" + interfaceClass.getName() + "] is not an interface");
        }
        if (!this.interfaces.contains(interfaceClass)) {
            this.interfaces.add(interfaceClass);
            adviceChanged();
        }
    }

    public boolean removeInterface(Class<?> interfaceClass) {
        return this.interfaces.remove(interfaceClass);
    }

    // advice变化时，清空方法缓存
    protected void adviceChanged() {
        this.methodCache.clear();
    }

    public TargetSource getTargetSource() {
        return targetSource;
    }

    public void setTargetSource(TargetSource targetSource) {
        this.targetSource = (targetSource != null ? targetSource : EMPTY_TARGET_SOURCE);
    }

    public Class<?> getTargetClass() {
        return this.targetSource.getTargetClass();
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetSource = new EmptyTargetSource(targetClass);
    }

    // 为target对象创建SingletonTargetSource
    public void setTarget(Object target) {
        setTargetSource(new SingletonTargetSource(target));
    }

    public AdvisorChainFactory getAdvisorChainFactory() {
        return advisorChainFactory;
    }

    public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
        this.advisorChainFactory = advisorChainFactory;
    }

    @Override
    public Class<?>[] getProxiedInterfaces() {
        return this.interfaces.toArray(new Class<?>[this.interfaces.size()]);
    }

    @Override
    public boolean isInterfaceProxied(Class<?> intf) {
        for (Class<?> proxyIntf : this.interfaces) {
            if (intf.isAssignableFrom(proxyIntf)) {
                return true;
            }
        }
        return false;
    }

    protected final void updateAdvisorArray() {
        this.advisorArray = this.advisors.toArray(new Advisor[this.advisors.size()]);
    }

    protected final List<Advisor> getAdvisorsInternal() {
        return this.advisors;
    }

    @Override
    public final Advisor[] getAdvisors() {
        return this.advisorArray;
    }

    @Override
    public void addAdvisor(Advisor advisor) {
        int pos = this.advisors.size();
        addAdvisor(pos, advisor);
    }

    @Override
    public void addAdvisor(int pos, Advisor advisor) {

        addAdvisorInternal(pos, advisor);
    }

    private void addAdvisorInternal(int pos, Advisor advisor) {
        Assert.notNull(advisor, "Advisor must not be null");

        if (pos > this.advisors.size()) {
            throw new IllegalArgumentException("Illegal position " + pos + " in advisor list with size " + this.advisors.size());
        }
        this.advisors.add(pos, advisor);
        updateAdvisorArray();
        adviceChanged();
    }

    @Override
    public boolean removeAdvisor(Advisor advisor) {
        int index = indexOf(advisor);
        if (index == -1) {
            return false;
        } else {
            removeAdvisor(index);
            return true;
        }
    }

    @Override
    public void removeAdvisor(int index) {

        if (index < 0 || index > this.advisors.size() - 1) {
            try {
                throw new Exception("Advisor index " + index + " is out of bounds: This configuration only has " + this.advisors.size() + " advisors.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Advisor advisor = this.advisors.get(index);

        this.advisors.remove(index);
        updateAdvisorArray();
        adviceChanged();
    }

    @Override
    public int indexOf(Advisor advisor) {
        Assert.notNull(advisor, "Advisor must not be null");
        return this.advisors.indexOf(advisor);
    }

    @Override
    public boolean replaceAdvisor(Advisor a, Advisor b) {
        Assert.notNull(a, "Advisor a must not be null");
        Assert.notNull(b, "Advisor b must not be null");
        int index = indexOf(a);
        if (index == -1) {
            return false;
        }
        removeAdvisor(index);
        addAdvisor(index, b);
        return true;
    }

    public void addAdvisors(Advisor... advisors) {
        addAdvisors(Arrays.asList(advisors));
    }


    public void addAdvisors(Collection<Advisor> advisors) {

        if (!advisors.isEmpty()) {
            for (Advisor advisor : advisors) {
                Assert.notNull(advisor, "Advisor must not be null");
                this.advisors.add(advisor);
            }
            updateAdvisorArray();
            adviceChanged();
        }
    }


    @Override
    public void addAdvice(Advice advice) {
        int pos = this.advisors.size();
        addAdvice(pos, advice);
    }

    // 添加advice默认会封装成DefaultPointcutAdvisor
    @Override
    public void addAdvice(int pos, Advice advice) {
        Assert.notNull(advice, "Advice must not be null");

        addAdvisor(pos, new DefaultPointcutAdvisor(advice));
    }

    @Override
    public boolean removeAdvice(Advice advice) {
        int index = indexOf(advice);
        if (index == -1) {
            return false;
        } else {
            removeAdvisor(index);
            return true;
        }
    }

    @Override
    public int indexOf(Advice advice) {
        Assert.notNull(advice, "Advice must not be null");
        for (int i = 0; i < this.advisors.size(); i++) {
            Advisor advisor = this.advisors.get(i);
            if (advisor.getAdvice() == advice) {
                return i;
            }
        }
        return -1;
    }


    public boolean adviceIncluded(Advice advice) {
        if (advice != null) {
            for (Advisor advisor : this.advisors) {
                if (advisor.getAdvice() == advice) {
                    return true;
                }
            }
        }
        return false;
    }


    public int countAdvicesOfType(Class<?> adviceClass) {
        int count = 0;
        if (adviceClass != null) {
            for (Advisor advisor : this.advisors) {
                if (adviceClass.isInstance(advisor.getAdvice())) {
                    count++;
                }
            }
        }
        return count;
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) {
        MethodCacheKey cacheKey = new MethodCacheKey(method);
        List<Object> cached = this.methodCache.get(cacheKey);
        if (cached == null) {
            cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(
                    this, method, targetClass);
            this.methodCache.put(cacheKey, cached);
        }
        return cached;
    }

}
