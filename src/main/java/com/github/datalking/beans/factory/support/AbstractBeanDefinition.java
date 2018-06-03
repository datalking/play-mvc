package com.github.datalking.beans.factory.support;

import com.github.datalking.beans.BeanMetadataAttributeAccessor;
import com.github.datalking.beans.MutablePropertyValues;
import com.github.datalking.beans.factory.config.AutowireCapableBeanFactory;
import com.github.datalking.beans.factory.config.BeanDefinition;
import com.github.datalking.beans.factory.config.ConstructorArgumentValues;
import com.github.datalking.common.MethodOverrides;
import com.github.datalking.util.Assert;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * BeanDefinition抽象类
 * beanName不在这里，在BeanDefinitionHolder
 *
 * @author yaoo on 4/3/18
 */
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor
        implements BeanDefinition, Cloneable {

    public static final String INFER_METHOD = "(inferred)";

    /// 依赖注入的方式
    public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;
    public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
    public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;
    public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;
    public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;

    // 默认就是候选bean
    private boolean autowireCandidate = true;

    // 默认不是主bean
    private boolean primary = false;

    private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<>(0);

    private ConstructorArgumentValues constructorArgumentValues;

    /**
     * 一般情况下，beanDefinitionReader阶段是字符串，createBean阶段是class对象
     */
    private volatile Object beanClass;

    private MutablePropertyValues propertyValues;

    // 懒加载默认false
    private boolean lazyInit = false;

    private boolean nonPublicAccessAllowed = true;

    // 默认false
    private boolean synthetic = false;

    // 确定构造函数是是否使用宽松构造的方式
    // 默认值true，即默认为宽松模式，即使多个构造函数的参数数量相同、类型存在父子类、接口实现类关系，也能正常创建bean
    private boolean lenientConstructorResolution = true;

    private String factoryBeanName;

    private String factoryMethodName;

    // 默认为0，即对应的bean不需要注入
    private int autowireMode;

    private int role = BeanDefinition.ROLE_APPLICATION;

    private MethodOverrides methodOverrides = new MethodOverrides();


//    private String[] dependsOn;
//    private String initMethodName;
//    private String destroyMethodName;
//    private Resource resource;
//    private boolean abstractFlag = false;

    public AbstractBeanDefinition() {
        setPropertyValues(null);
    }

    protected AbstractBeanDefinition(BeanDefinition original) {
        setBeanClassName(original.getBeanClassName());
        setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
        setLazyInit(original.isLazyInit());
        setFactoryBeanName(original.getFactoryBeanName());
        setFactoryMethodName(original.getFactoryMethodName());

        //  拷贝其他字段
        if (original instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
            if (originalAbd.hasBeanClass()) {
                setBeanClass(originalAbd.getBeanClass());
            }
        }
    }

    /**
     * 获取autowire的方式
     */
    public int getResolvedAutowireMode() {

        if (this.autowireMode == AUTOWIRE_AUTODETECT) {
            // Work out whether to apply setter autowiring or constructor autowiring.
            // If it has a no-arg constructor it's deemed to be setter autowiring,
            // otherwise we'll try constructor autowiring.
            Constructor<?>[] constructors = getBeanClass().getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterTypes().length == 0) {
                    return AUTOWIRE_BY_TYPE;
                }
            }
            return AUTOWIRE_CONSTRUCTOR;
        } else {
            return this.autowireMode;
        }
    }

    public Class<?> getBeanClass() throws IllegalStateException {
        Object beanClassObject = this.beanClass;
        if (beanClassObject == null) {
            throw new IllegalStateException("No bean class specified on bean definition");
        }

        // 类尚未加载，抛出异常
        if (!(beanClassObject instanceof Class)) {
            throw new IllegalStateException("Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
        }
        return (Class<?>) beanClassObject;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public String getBeanClassName() {
        Object beanClassObject = this.beanClass;
        if (beanClassObject instanceof Class) {
            return ((Class<?>) beanClassObject).getName();
        } else {
            return (String) beanClassObject;
        }
    }


    @Override
    public void setBeanClassName(String beanClassName) {
        this.beanClass = beanClassName;
    }

    public boolean hasBeanClass() {
        return (this.beanClass instanceof Class);
    }

    @Override
    public MutablePropertyValues getPropertyValues() {
        return this.propertyValues;
    }

    public void setPropertyValues(MutablePropertyValues propertyValues) {
        this.propertyValues = (propertyValues != null ? propertyValues : new MutablePropertyValues());
    }

    @Override
    public boolean isLazyInit() {
        return this.lazyInit;
    }

    @Override
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    public boolean isSynthetic() {
        return this.synthetic;
    }

//    @Override
//    public void setDependsOn(String... dependsOn) {
//        this.dependsOn = dependsOn;
//    }
//
//
//    @Override
//    public String[] getDependsOn() {
//        return this.dependsOn;
//    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getFactoryMethodName() {
        return factoryMethodName;
    }

    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    public ConstructorArgumentValues getConstructorArgumentValues() {
        return this.constructorArgumentValues;
    }

    public int getAutowireMode() {
        return this.autowireMode;
    }

    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    public boolean isAutowireCandidate() {
        return this.autowireCandidate;
    }

    public void setAutowireCandidate(boolean autowireCandidate) {
        this.autowireCandidate = autowireCandidate;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    public void addQualifier(AutowireCandidateQualifier qualifier) {
        this.qualifiers.put(qualifier.getTypeName(), qualifier);
    }

    public boolean hasQualifier(String typeName) {
        return this.qualifiers.keySet().contains(typeName);
    }

    public AutowireCandidateQualifier getQualifier(String typeName) {
        return this.qualifiers.get(typeName);
    }

    public Set<AutowireCandidateQualifier> getQualifiers() {
        return new LinkedHashSet<>(this.qualifiers.values());
    }

    public void copyQualifiersFrom(AbstractBeanDefinition source) {
        Assert.notNull(source, "Source must not be null");
        this.qualifiers.putAll(source.qualifiers);
    }

    public int getRole() {
        return this.role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public boolean isNonPublicAccessAllowed() {
        return this.nonPublicAccessAllowed;
    }

    public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
        this.nonPublicAccessAllowed = nonPublicAccessAllowed;
    }

    public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
        this.lenientConstructorResolution = lenientConstructorResolution;
    }

    public boolean isLenientConstructorResolution() {
        return this.lenientConstructorResolution;
    }

    public void setMethodOverrides(MethodOverrides methodOverrides) {
        this.methodOverrides = (methodOverrides != null ? methodOverrides : new MethodOverrides());
    }

    public MethodOverrides getMethodOverrides() {
        return this.methodOverrides;
    }

    @Override
    public Object clone() {
        return cloneBeanDefinition();
    }


    public abstract AbstractBeanDefinition cloneBeanDefinition();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractBeanDefinition that = (AbstractBeanDefinition) o;

        if (!beanClass.equals(that.beanClass)) return false;
        // if (lazyInit != that.lazyInit) return false;
        if (factoryBeanName != null ? !factoryBeanName.equals(that.factoryBeanName) : that.factoryBeanName != null)
            return false;
        return factoryMethodName != null ? factoryMethodName.equals(that.factoryMethodName) : that.factoryMethodName == null;
    }

    @Override
    public int hashCode() {
        int result = beanClass.hashCode();
        // result = 31 * result + (lazyInit ? 1 : 0);
        result = 31 * result + (factoryBeanName != null ? factoryBeanName.hashCode() : 0);
        result = 31 * result + (factoryMethodName != null ? factoryMethodName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "beanClass=" + beanClass +
                ", factoryBeanName='" + factoryBeanName + '\'' +
                ", factoryMethodName='" + factoryMethodName + '\'' +
                ", lazyInit=" + lazyInit +
                '}';
    }
}
