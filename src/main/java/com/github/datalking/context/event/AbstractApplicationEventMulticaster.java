package com.github.datalking.context.event;

import com.github.datalking.beans.factory.BeanFactory;
import com.github.datalking.beans.factory.BeanFactoryAware;
import com.github.datalking.beans.factory.config.ConfigurableBeanFactory;
import com.github.datalking.beans.factory.support.AbstractBeanFactory;
import com.github.datalking.common.OrderComparator;
import com.github.datalking.context.ApplicationEvent;
import com.github.datalking.context.ApplicationListener;
import com.github.datalking.util.ObjectUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yaoo on 6/2/18
 */
public abstract class AbstractApplicationEventMulticaster
//        implements ApplicationEventMulticaster, BeanClassLoaderAware, BeanFactoryAware {
        implements ApplicationEventMulticaster, BeanFactoryAware {

    private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);

    private final Map<ListenerCacheKey, ListenerRetriever> retrieverCache = new ConcurrentHashMap<>(64);

    private ClassLoader beanClassLoader;

    private BeanFactory beanFactory;

    private Object retrievalMutex = this.defaultRetriever;

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        if (this.beanClassLoader == null && beanFactory instanceof ConfigurableBeanFactory) {
//            this.beanClassLoader = ((ConfigurableBeanFactory) beanFactory).getBeanClassLoader();
            this.beanClassLoader = this.getClass().getClassLoader();
        }
        if (beanFactory instanceof AbstractBeanFactory) {
//            this.retrievalMutex = ((AbstractBeanFactory) beanFactory).getSingletonMutex();
        }
    }

    private BeanFactory getBeanFactory() {
        if (this.beanFactory == null) {
            throw new IllegalStateException("ApplicationEventMulticaster cannot retrieve listener beans because it is not associated with a BeanFactory");
        }
        return this.beanFactory;
    }


    public void addApplicationListener(ApplicationListener listener) {
        synchronized (this.retrievalMutex) {
            this.defaultRetriever.applicationListeners.add(listener);
            this.retrieverCache.clear();
        }
    }

    public void addApplicationListenerBean(String listenerBeanName) {
        synchronized (this.retrievalMutex) {
            this.defaultRetriever.applicationListenerBeans.add(listenerBeanName);
            this.retrieverCache.clear();
        }
    }

    public void removeApplicationListener(ApplicationListener listener) {
        synchronized (this.retrievalMutex) {
            this.defaultRetriever.applicationListeners.remove(listener);
            this.retrieverCache.clear();
        }
    }

    public void removeApplicationListenerBean(String listenerBeanName) {
        synchronized (this.retrievalMutex) {
            this.defaultRetriever.applicationListenerBeans.remove(listenerBeanName);
            this.retrieverCache.clear();
        }
    }

    public void removeAllListeners() {
        synchronized (this.retrievalMutex) {
            this.defaultRetriever.applicationListeners.clear();
            this.defaultRetriever.applicationListenerBeans.clear();
            this.retrieverCache.clear();
        }
    }


    /**
     * Return a Collection containing all ApplicationListeners.
     *
     * @return a Collection of ApplicationListeners
     */
    protected Collection<ApplicationListener> getApplicationListeners() {
        synchronized (this.retrievalMutex) {
            return this.defaultRetriever.getApplicationListeners();
        }
    }

    /**
     * Return a Collection of ApplicationListeners matching the given event type.
     * Non-matching listeners get excluded early.
     *
     * @param event the event to be propagated.
     *              Allows for excluding non-matching listeners early, based on cached matching information.
     * @return a Collection of ApplicationListeners
     */
    protected Collection<ApplicationListener> getApplicationListeners(ApplicationEvent event) {
        Class<? extends ApplicationEvent> eventType = event.getClass();
        Object source = event.getSource();
        Class<?> sourceType = (source != null ? source.getClass() : null);
        ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);

        // Quick check for existing entry on ConcurrentHashMap...
        ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
        if (retriever != null) {
            return retriever.getApplicationListeners();
        }

//        if (this.beanClassLoader == null ||
//                (ClassUtils.isCacheSafe(eventType, this.beanClassLoader) &&
//                        (sourceType == null || ClassUtils.isCacheSafe(sourceType, this.beanClassLoader)))) {
//            // Fully synchronized building and caching of a ListenerRetriever
//            synchronized (this.retrievalMutex) {
//                retriever = this.retrieverCache.get(cacheKey);
//                if (retriever != null) {
//                    return retriever.getApplicationListeners();
//                }
//                retriever = new ListenerRetriever(true);
//                Collection<ApplicationListener> listeners =
//                        retrieveApplicationListeners(eventType, sourceType, retriever);
//                this.retrieverCache.put(cacheKey, retriever);
//                return listeners;
//            }
//        } else {
        // No ListenerRetriever caching -> no synchronization necessary
        return retrieveApplicationListeners(eventType, sourceType, null);
//        }
    }

    /**
     * Actually retrieve the application listeners for the given event and source type.
     *
     * @param eventType  the application event type
     * @param sourceType the event source type
     * @param retriever  the ListenerRetriever, if supposed to populate one (for caching purposes)
     * @return the pre-filtered list of application listeners for the given event and source type
     */
    private Collection<ApplicationListener> retrieveApplicationListeners(
            Class<? extends ApplicationEvent> eventType, Class<?> sourceType, ListenerRetriever retriever) {

        LinkedList<ApplicationListener> allListeners = new LinkedList<>();
        Set<ApplicationListener> listeners;
        Set<String> listenerBeans;

        synchronized (this.retrievalMutex) {
            listeners = new LinkedHashSet<>(this.defaultRetriever.applicationListeners);
            listenerBeans = new LinkedHashSet<>(this.defaultRetriever.applicationListenerBeans);
        }

        for (ApplicationListener listener : listeners) {
            if (supportsEvent(listener, eventType, sourceType)) {
                if (retriever != null) {
                    retriever.applicationListeners.add(listener);
                }
                allListeners.add(listener);
            }
        }
        if (!listenerBeans.isEmpty()) {
            BeanFactory beanFactory = getBeanFactory();

            for (String listenerBeanName : listenerBeans) {
//                ApplicationListener listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                ApplicationListener listener = (ApplicationListener) beanFactory.getBean(listenerBeanName);
                if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
                    if (retriever != null) {
                        retriever.applicationListenerBeans.add(listenerBeanName);
                    }
                    allListeners.add(listener);
                }
            }
        }
        OrderComparator.sort(allListeners);
        return allListeners;
    }

    /**
     * Determine whether the given listener supports the given event.
     * <p>The default implementation detects the  SmartApplicationListener
     * interface. In case of a standard {@link ApplicationListener}, a
     * GenericApplicationListenerAdapter will be used to introspect
     * the generically declared type of the target listener.
     *
     * @param listener   the target listener to check
     * @param eventType  the event type to check against
     * @param sourceType the source type to check against
     * @return whether the given listener should be included in the candidates for the given event type
     */
    protected boolean supportsEvent(ApplicationListener listener, Class<? extends ApplicationEvent> eventType, Class<?> sourceType) {

        SmartApplicationListener smartListener = (listener instanceof SmartApplicationListener ?
                (SmartApplicationListener) listener : new GenericApplicationListenerAdapter(listener));
        return (smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType));
    }


    /**
     * Cache key for ListenerRetrievers, based on event type and source type.
     */
    private static class ListenerCacheKey {

        private final Class<?> eventType;

        private final Class<?> sourceType;

        public ListenerCacheKey(Class<?> eventType, Class<?> sourceType) {
            this.eventType = eventType;
            this.sourceType = sourceType;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            ListenerCacheKey otherKey = (ListenerCacheKey) other;
            return ObjectUtils.nullSafeEquals(this.eventType, otherKey.eventType) &&
                    ObjectUtils.nullSafeEquals(this.sourceType, otherKey.sourceType);
        }

        @Override
        public int hashCode() {
            return ObjectUtils.nullSafeHashCode(this.eventType) * 29 + ObjectUtils.nullSafeHashCode(this.sourceType);
        }
    }


    /**
     * Helper class that encapsulates a specific set of target listeners,
     * allowing for efficient retrieval of pre-filtered listeners.
     * <p>An instance of this helper gets cached per event type and source type.
     */
    private class ListenerRetriever {

        public final Set<ApplicationListener> applicationListeners;

        public final Set<String> applicationListenerBeans;

        private final boolean preFiltered;

        public ListenerRetriever(boolean preFiltered) {
            this.applicationListeners = new LinkedHashSet<>();
            this.applicationListenerBeans = new LinkedHashSet<>();
            this.preFiltered = preFiltered;
        }

        public Collection<ApplicationListener> getApplicationListeners() {
            LinkedList<ApplicationListener> allListeners = new LinkedList<>();
            for (ApplicationListener listener : this.applicationListeners) {
                allListeners.add(listener);
            }

            if (!this.applicationListenerBeans.isEmpty()) {
                BeanFactory beanFactory = getBeanFactory();
                for (String listenerBeanName : this.applicationListenerBeans) {
//                    ApplicationListener listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
                    ApplicationListener listener = (ApplicationListener) beanFactory.getBean(listenerBeanName);
                    if (this.preFiltered || !allListeners.contains(listener)) {
                        allListeners.add(listener);
                    }
                }
            }
            OrderComparator.sort(allListeners);
            return allListeners;
        }
    }

}
