package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.factory.ObjectFactory;
import com.github.datalking.beans.factory.config.SingletonBeanRegistry;
import com.github.datalking.util.Assert;
import com.github.datalking.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 生成的单例bean的集合
 *
 * @author yaoo on 4/4/18
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    /**
     * 空对象标志，解决ConcurrentHashMap不支持null的问题
     */
    private static final Object NULL_OBJECT = new Object();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 正在创建中的bean名称 集合
     */
    private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>(16));
    /**
     * 所有的单例bean 一级缓存
     */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    /**
     * 调用构造方法之后，属性初始化之前会把对象放入到这里 二级缓存
     */
    private final Map<String, Object> earlySingletonObjects = new HashMap<String, Object>(16);
    /**
     * 存在循环依赖的bean 三级缓存
     * 把构造成功，但属性还没注入的bean放到ObjectFactory
     */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<String, ObjectFactory<?>>(16);

    private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

//    例子
//    <bean id="A" class="com.demo.app.Cat" depends-on="B;C"></bean>
//    <bean id="B" class="com.demo.app.Cat"></bean>
//    <bean id="C" class="com.demo.app.Cat" depends-on="B;D"></bean>
//    <bean id="D" class="com.demo.app.Cat"></bean>
//
//    dependenciesForBeanMap
//    {A=[B,C], C=[B,D]}
//
//    dependentBeanMap
//    {B=[A,C],C=[A],D=[C]}

    /**
     * ConcurrentHashMap
     * key-bean名
     * value-key所依赖的bean
     * Map between depending bean names: bean name --> Set of bean names for the bean's dependencies
     */
    private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);


    /**
     * ConcurrentHashMap
     * key-bean名
     * value-依赖key的bean
     * Map between dependent bean names: bean name --> Set of dependent bean names
     */
    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

//private final Map<String, Object> disposableBeans = new LinkedHashMap<String, Object>();


    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        Assert.notNull(beanName, "'beanName' must not be null");
        synchronized (this.singletonObjects) {
            Object oldObject = this.singletonObjects.get(beanName);
            //不能注册同名bean
            if (oldObject != null) {
                throw new IllegalStateException("Could not register object [" + singletonObject +
                        "] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
            }
            addSingleton(beanName, singletonObject);
        }
    }

    protected void addSingleton(String beanName, Object singletonObject) {

        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, (singletonObject != null ? singletonObject : NULL_OBJECT));
            this.registeredSingletons.add(beanName);

            // 清除二级、三级缓存
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
        }

    }

    // 用于提前注册bean，避免循环依赖
    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(singletonFactory, "Singleton factory must not be null");
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {

                this.singletonFactories.put(beanName, singletonFactory);
                this.registeredSingletons.add(beanName);

                this.earlySingletonObjects.remove(beanName);
            }
        }
    }

    @Override
    public Object getSingleton(String beanName) {
        return getSingleton(beanName, true);

    }

    /**
     * 通过beanName获取bean实例
     * 在所有三级缓存中找bean实例：singletonObjects > earlySingletonObjects > singletonFactories
     *
     * @param beanName            bean名称
     * @param allowEarlyReference 是否允许在第3级缓存中查找
     * @return bean实例
     */
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {

        Object singletonObject = this.singletonObjects.get(beanName);

        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {

            synchronized (this.singletonObjects) {

                singletonObject = this.earlySingletonObjects.get(beanName);

                if (singletonObject == null && allowEarlyReference) {

                    ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        singletonObject = singletonFactory.getObject();

                        /// 如果在工厂中找到了，就提升到二级缓存
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }

        return (singletonObject != NULL_OBJECT ? singletonObject : null);
    }

    /**
     * 从ObjectFactory工厂获取bean
     *
     * @param beanName         bean名称
     * @param singletonFactory ObjectFactory
     * @return bean实例
     */
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "'beanName' must not be null");

        synchronized (this.singletonObjects) {

            Object singletonObject = this.singletonObjects.get(beanName);

            if (singletonObject == null) {
                beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                try {

                    singletonObject = singletonFactory.getObject();
                    newSingleton = true;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    afterSingletonCreation(beanName);

                }

                /// 添加bean到map中
                if (newSingleton) {
                    addSingleton(beanName, singletonObject);
                }
            }

            return (singletonObject != NULL_OBJECT ? singletonObject : null);
        }

    }

    protected void removeSingleton(String beanName) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.remove(beanName);
        }
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return this.singletonObjects.containsKey(beanName);
    }

    @Override
    public String[] getSingletonNames() {
        //todo
        return new String[0];
    }

    @Override
    public int getSingletonCount() {
        synchronized (this.singletonObjects) {
            return this.singletonObjects.size();
//            return this.registeredSingletons.size();
        }
    }


    public boolean isCurrentlyInCreation(String beanName) {
        Assert.notNull(beanName, "Bean name must not be null");
        return isSingletonCurrentlyInCreation(beanName);
    }

    public boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }

    protected void beforeSingletonCreation(String beanName) {
        this.singletonsCurrentlyInCreation.add(beanName);
    }

    protected void afterSingletonCreation(String beanName) {
        this.singletonsCurrentlyInCreation.remove(beanName);
    }

    public void registerDependentBean(String beanName, String dependentBeanName) {
//        String canonicalName = canonicalName(beanName);
        String canonicalName = beanName;

        synchronized (this.dependentBeanMap) {
            Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
            if (dependentBeans == null) {
                dependentBeans = new LinkedHashSet<>(8);
                this.dependentBeanMap.put(canonicalName, dependentBeans);
            }
            dependentBeans.add(dependentBeanName);
        }

        synchronized (this.dependenciesForBeanMap) {
            Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(dependentBeanName);
            if (dependenciesForBean == null) {
                dependenciesForBean = new LinkedHashSet<>(8);
                this.dependenciesForBeanMap.put(dependentBeanName, dependenciesForBean);
            }
            dependenciesForBean.add(canonicalName);
        }

    }

    public String[] getDependentBeans(String beanName) {
        Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
        if (dependentBeans == null) {
            return new String[0];
        }
        return StringUtils.toStringArray(dependentBeans);
    }

    public String[] getDependenciesForBean(String beanName) {
        Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(beanName);
        if (dependenciesForBean == null) {
            return new String[0];
        }
        return dependenciesForBean.toArray(new String[dependenciesForBean.size()]);
    }


//    public void destroySingleton(String beanName) {
//        removeSingleton(beanName);
//
//        // Destroy the corresponding DisposableBean instance.
//        DisposableBean disposableBean;
//        synchronized (this.disposableBeans) {
//            disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
//        }
//        destroyBean(beanName, disposableBean);
//    }

    // 要先destroy依赖的bean
//    protected void destroyBean(String beanName, DisposableBean bean) {
//    }

}
