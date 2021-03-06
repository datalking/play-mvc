package com.github.datalking.context.annotation;

import com.github.datalking.beans.MutablePropertyValues;
import com.github.datalking.beans.PropertyValues;
import com.github.datalking.beans.factory.support.RootBeanDefinition;
import com.github.datalking.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * autowire依赖项的元数据
 *
 * @author yaoo on 5/28/18
 */
public class InjectionMetadata {

    private static final Logger logger = LoggerFactory.getLogger(InjectionMetadata.class);

    private final Class<?> targetClass;

    private final Collection<InjectedElement> injectedElements;

    private volatile Set<InjectedElement> checkedElements;

    public InjectionMetadata(Class<?> targetClass, Collection<InjectedElement> elements) {
        this.targetClass = targetClass;
        this.injectedElements = elements;
    }

    public void checkConfigMembers(RootBeanDefinition beanDefinition) {

        Set<InjectedElement> checkedElements = new LinkedHashSet<>(this.injectedElements.size());

        for (InjectedElement element : this.injectedElements) {
            // 获取字段或方法
            Member member = element.getMember();
            if (!beanDefinition.isExternallyManagedConfigMember(member)) {
                beanDefinition.registerExternallyManagedConfigMember(member);
                checkedElements.add(element);
            }
        }
        this.checkedElements = checkedElements;
    }
    // 将元数据注入targetBean
    public void inject(Object target, String beanName, PropertyValues pvs) throws Throwable {
        Collection<InjectedElement> elementsToIterate =
                (this.checkedElements != null ? this.checkedElements : this.injectedElements);

        if (!elementsToIterate.isEmpty()) {

            for (InjectedElement element : elementsToIterate) {
                element.inject(target, beanName, pvs);
            }
        }
    }

    public void clear(PropertyValues pvs) {
        Collection<InjectedElement> elementsToIterate =
                (this.checkedElements != null ? this.checkedElements : this.injectedElements);
        if (!elementsToIterate.isEmpty()) {
            for (InjectedElement element : elementsToIterate) {
                element.clearPropertySkipping(pvs);
            }
        }
    }

    public static boolean needsRefresh(InjectionMetadata metadata, Class<?> clazz) {
        return (metadata == null || !metadata.targetClass.equals(clazz));
    }

    public static abstract class InjectedElement {

        protected final Member member;

        protected final boolean isField;

        protected final PropertyDescriptor pd;

        protected volatile Boolean skip;

        protected InjectedElement(Member member, PropertyDescriptor pd) {
            this.member = member;
            this.isField = (member instanceof Field);
            this.pd = pd;
        }

        public final Member getMember() {
            return this.member;
        }

        protected final Class<?> getResourceType() {
            if (this.isField) {
                return ((Field) this.member).getType();
            } else if (this.pd != null) {
                return this.pd.getPropertyType();
            } else {
                return ((Method) this.member).getParameterTypes()[0];
            }
        }

        protected final void checkResourceType(Class<?> resourceType) {
            if (this.isField) {
                Class<?> fieldType = ((Field) this.member).getType();
                if (!(resourceType.isAssignableFrom(fieldType) || fieldType.isAssignableFrom(resourceType))) {
                    throw new IllegalStateException("Specified field type [" + fieldType +
                            "] is incompatible with resource type [" + resourceType.getName() + "]");
                }
            } else {
                Class<?> paramType =
                        (this.pd != null ? this.pd.getPropertyType() : ((Method) this.member).getParameterTypes()[0]);
                if (!(resourceType.isAssignableFrom(paramType) || paramType.isAssignableFrom(resourceType))) {
                    throw new IllegalStateException("Specified parameter type [" + paramType +
                            "] is incompatible with resource type [" + resourceType.getName() + "]");
                }
            }
        }

        /**
         * Either this or {@link #getResourceToInject} needs to be overridden.
         */
        protected void inject(Object target, String requestingBeanName, PropertyValues pvs) throws Throwable {

            if (this.isField) {
                Field field = (Field) this.member;
                ReflectionUtils.makeAccessible(field);
                field.set(target, getResourceToInject(target, requestingBeanName));
            } else {
                if (checkPropertySkipping(pvs)) {
                    return;
                }
                try {
                    Method method = (Method) this.member;
                    ReflectionUtils.makeAccessible(method);
                    method.invoke(target, getResourceToInject(target, requestingBeanName));
                } catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
            }
        }

        protected boolean checkPropertySkipping(PropertyValues pvs) {
            if (this.skip != null) {
                return this.skip;
            }
            if (pvs == null) {
                this.skip = false;
                return false;
            }
            synchronized (pvs) {
                if (this.skip != null) {
                    return this.skip;
                }
                if (this.pd != null) {
                    if (pvs.contains(this.pd.getName())) {
                        // Explicit value provided as part of the bean definition.
                        this.skip = true;
                        return true;
                    } else if (pvs instanceof MutablePropertyValues) {
                        ((MutablePropertyValues) pvs).registerProcessedProperty(this.pd.getName());
                    }
                }
                this.skip = false;
                return false;
            }
        }

        protected void clearPropertySkipping(PropertyValues pvs) {
            if (pvs == null) {
                return;
            }
            synchronized (pvs) {
                if (Boolean.FALSE.equals(this.skip) && this.pd != null && pvs instanceof MutablePropertyValues) {
                    ((MutablePropertyValues) pvs).clearProcessedProperty(this.pd.getName());
                }
            }
        }

        protected Object getResourceToInject(Object target, String requestingBeanName) {
            return null;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof InjectedElement)) {
                return false;
            }
            InjectedElement otherElement = (InjectedElement) other;
            return this.member.equals(otherElement.member);
        }

        @Override
        public int hashCode() {
            return this.member.getClass().hashCode() * 29 + this.member.getName().hashCode();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " for " + this.member;
        }
    }

}
