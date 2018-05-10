package com.github.datalking.common;

import com.github.datalking.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author yaoo on 5/10/18
 */
public abstract class CollectionFactory {

    private static Class navigableSetClass = null;

    private static Class navigableMapClass = null;

    private static final Set<Class> approximableCollectionTypes = new HashSet<Class>(10);

    private static final Set<Class> approximableMapTypes = new HashSet<Class>(6);

    static {
        // Standard collection interfaces
        approximableCollectionTypes.add(Collection.class);
        approximableCollectionTypes.add(List.class);
        approximableCollectionTypes.add(Set.class);
        approximableCollectionTypes.add(SortedSet.class);
        approximableMapTypes.add(Map.class);
        approximableMapTypes.add(SortedMap.class);

        // New Java 6 collection interfaces
        ClassLoader cl = CollectionFactory.class.getClassLoader();
        try {
            navigableSetClass = ClassUtils.forName("java.util.NavigableSet", cl);
            navigableMapClass = ClassUtils.forName("java.util.NavigableMap", cl);
            approximableCollectionTypes.add(navigableSetClass);
            approximableMapTypes.add(navigableMapClass);
        } catch (ClassNotFoundException ex) {
            // not running on Java 6 or above...
        }

        // Common concrete collection classes
        approximableCollectionTypes.add(ArrayList.class);
        approximableCollectionTypes.add(LinkedList.class);
        approximableCollectionTypes.add(HashSet.class);
        approximableCollectionTypes.add(LinkedHashSet.class);
        approximableCollectionTypes.add(TreeSet.class);
        approximableMapTypes.add(HashMap.class);
        approximableMapTypes.add(LinkedHashMap.class);
        approximableMapTypes.add(TreeMap.class);
    }


    @Deprecated
    public static <T> Set<T> createLinkedSetIfPossible(int initialCapacity) {
        return new LinkedHashSet<>(initialCapacity);
    }

    @Deprecated
    public static <T> Set<T> createCopyOnWriteSet() {
        return new CopyOnWriteArraySet<T>();
    }

    @Deprecated
    public static <K, V> Map<K, V> createLinkedMapIfPossible(int initialCapacity) {
        return new LinkedHashMap<K, V>(initialCapacity);
    }

    @Deprecated
    public static Map createLinkedCaseInsensitiveMapIfPossible(int initialCapacity) {
        return new LinkedCaseInsensitiveMap(initialCapacity);
    }

    @Deprecated
    public static Map createIdentityMapIfPossible(int initialCapacity) {
        return new IdentityHashMap(initialCapacity);
    }

    @Deprecated
    public static Map createConcurrentMapIfPossible(int initialCapacity) {
        return new ConcurrentHashMap(initialCapacity);
    }

    @Deprecated
    public static ConcurrentMap createConcurrentMap(int initialCapacity) {
        return new JdkConcurrentHashMap(initialCapacity);
    }

    public static boolean isApproximableCollectionType(Class<?> collectionType) {
        return (collectionType != null && approximableCollectionTypes.contains(collectionType));
    }

    public static Collection createApproximateCollection(Object collection, int initialCapacity) {
        if (collection instanceof LinkedList) {
            return new LinkedList();
        } else if (collection instanceof List) {
            return new ArrayList(initialCapacity);
        } else if (collection instanceof SortedSet) {
            return new TreeSet(((SortedSet) collection).comparator());
        } else {
            return new LinkedHashSet(initialCapacity);
        }
    }

    public static Collection createCollection(Class<?> collectionType, int initialCapacity) {
        if (collectionType.isInterface()) {
            if (List.class.equals(collectionType)) {
                return new ArrayList(initialCapacity);
            } else if (SortedSet.class.equals(collectionType) || collectionType.equals(navigableSetClass)) {
                return new TreeSet();
            } else if (Set.class.equals(collectionType) || Collection.class.equals(collectionType)) {
                return new LinkedHashSet(initialCapacity);
            } else {
                throw new IllegalArgumentException("Unsupported Collection interface: " + collectionType.getName());
            }
        } else {
            if (!Collection.class.isAssignableFrom(collectionType)) {
                throw new IllegalArgumentException("Unsupported Collection type: " + collectionType.getName());
            }
            try {
                return (Collection) collectionType.newInstance();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Could not instantiate Collection type: " +
                        collectionType.getName(), ex);
            }
        }
    }

    public static boolean isApproximableMapType(Class<?> mapType) {
        return (mapType != null && approximableMapTypes.contains(mapType));
    }

    public static Map createApproximateMap(Object map, int initialCapacity) {
        if (map instanceof SortedMap) {
            return new TreeMap(((SortedMap) map).comparator());
        } else {
            return new LinkedHashMap(initialCapacity);
        }
    }

    public static Map createMap(Class<?> mapType, int initialCapacity) {
        if (mapType.isInterface()) {
            if (Map.class.equals(mapType)) {
                return new LinkedHashMap(initialCapacity);
            } else if (SortedMap.class.equals(mapType) || mapType.equals(navigableMapClass)) {
                return new TreeMap();
            } else if (MultiValueMap.class.equals(mapType)) {
                return new LinkedMultiValueMap();
            } else {
                throw new IllegalArgumentException("Unsupported Map interface: " + mapType.getName());
            }
        } else {
            if (!Map.class.isAssignableFrom(mapType)) {
                throw new IllegalArgumentException("Unsupported Map type: " + mapType.getName());
            }
            try {
                return (Map) mapType.newInstance();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Could not instantiate Map type: " +
                        mapType.getName(), ex);
            }
        }
    }

    @Deprecated
    private static class JdkConcurrentHashMap extends ConcurrentHashMap implements ConcurrentMap {

        private JdkConcurrentHashMap(int initialCapacity) {
            super(initialCapacity);
        }
    }

}
