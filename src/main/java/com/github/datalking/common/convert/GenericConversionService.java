package com.github.datalking.common.convert;

import com.github.datalking.common.GenericTypeResolver;
import com.github.datalking.common.convert.converter.ConditionalConverter;
import com.github.datalking.common.convert.converter.ConditionalGenericConverter;
import com.github.datalking.common.convert.converter.Converter;
import com.github.datalking.common.convert.converter.ConverterFactory;
import com.github.datalking.common.convert.converter.ConverterRegistry;
import com.github.datalking.common.convert.converter.GenericConverter;
import com.github.datalking.common.convert.converter.GenericConverter.ConvertiblePair;
import com.github.datalking.common.convert.descriptor.TypeDescriptor;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ClassUtils;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yaoo on 5/10/18
 */
public class GenericConversionService implements ConversionService, ConverterRegistry {

    private static final GenericConverter NO_OP_CONVERTER = new NoOpConverter("NO_OP");

    private static final GenericConverter NO_MATCH = new NoOpConverter("NO_MATCH");

    private final Converters converters = new Converters();

    private final Map<ConverterCacheKey, GenericConverter> converterCache = new ConcurrentHashMap<>(64);

    // implementing ConverterRegistry

    @Override
    public void addConverter(Converter<?, ?> converter) {
        GenericConverter.ConvertiblePair typeInfo = getRequiredTypeInfo(converter, Converter.class);
        if (typeInfo == null) {
            throw new IllegalArgumentException("Unable to determine source typeand target typefor "+ converter.getClass().getName());
        }
        addConverter(new ConverterAdapter(converter, typeInfo));
    }

    @Override
    public void addConverter(Class<?> sourceType, Class<?> targetType, Converter<?, ?> converter) {
        GenericConverter.ConvertiblePair typeInfo = new GenericConverter.ConvertiblePair(sourceType, targetType);
        addConverter(new ConverterAdapter(converter, typeInfo));
    }

    @Override
    public void addConverter(GenericConverter converter) {
        this.converters.add(converter);
        invalidateCache();
    }

    @Override
    public void addConverterFactory(ConverterFactory<?, ?> factory) {
        GenericConverter.ConvertiblePair typeInfo = getRequiredTypeInfo(factory, ConverterFactory.class);
        if (typeInfo == null) {
            throw new IllegalArgumentException("Unable to determine source typeand target typefor " + factory.getClass().getName());
        }
        addConverter(new ConverterFactoryAdapter(factory, typeInfo));
    }

    @Override
    public void removeConvertible(Class<?> sourceType, Class<?> targetType) {
        this.converters.remove(sourceType, targetType);
        invalidateCache();
    }

    // implementing ConversionService

    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        Assert.notNull(targetType, "targetType to convert to cannot be null");
        return canConvert((sourceType != null ? TypeDescriptor.valueOf(sourceType) : null),
                TypeDescriptor.valueOf(targetType));
    }

    public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
        Assert.notNull(targetType, "targetType to convert to cannot be null");
        if (sourceType == null) {
            return true;
        }
        GenericConverter converter = getConverter(sourceType, targetType);
        return (converter != null);
    }

    public boolean canBypassConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
        Assert.notNull(targetType, "targetType to convert to cannot be null");
        if (sourceType == null) {
            return true;
        }
        GenericConverter converter = getConverter(sourceType, targetType);
        return (converter == NO_OP_CONVERTER);
    }

    public <T> T convert(Object source, Class<T> targetType) {
        Assert.notNull(targetType, "targetType to convert to cannot be null");
        return (T) convert(source, TypeDescriptor.forObject(source), TypeDescriptor.valueOf(targetType));
    }

    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Assert.notNull(targetType, "targetType to convert to cannot be null");
        if (sourceType == null) {
            Assert.isTrue(source == null, "source must be [null] if sourceType == [null]");
            return handleResult(null, targetType, convertNullSource(null, targetType));
        }
        if (source != null && !sourceType.getObjectType().isInstance(source)) {
            throw new IllegalArgumentException("source to convert from must be an instance of " +
                    sourceType + "; instead it was a " + source.getClass().getName());
        }
        GenericConverter converter = getConverter(sourceType, targetType);
        if (converter != null) {
            Object result = ConversionUtils.invokeConverter(converter, source, sourceType, targetType);
            return handleResult(sourceType, targetType, result);
        }
        return handleConverterNotFound(source, sourceType, targetType);
    }


    public Object convert(Object source, TypeDescriptor targetType) {
        return convert(source, TypeDescriptor.forObject(source), targetType);
    }

    @Override
    public String toString() {
        return this.converters.toString();
    }


    // Protected template methods

    protected Object convertNullSource(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return null;
    }

    protected GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
        ConverterCacheKey key = new ConverterCacheKey(sourceType, targetType);
        GenericConverter converter = this.converterCache.get(key);
        if (converter != null) {
            return (converter != NO_MATCH ? converter : null);
        }

        converter = this.converters.find(sourceType, targetType);
        if (converter == null) {
            converter = getDefaultConverter(sourceType, targetType);
        }

        if (converter != null) {
            this.converterCache.put(key, converter);
            return converter;
        }

        this.converterCache.put(key, NO_MATCH);
        return null;
    }

    protected GenericConverter getDefaultConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return (sourceType.isAssignableTo(targetType) ? NO_OP_CONVERTER : null);
    }

    // internal helpers

    private GenericConverter.ConvertiblePair getRequiredTypeInfo(Object converter, Class<?> genericIfc) {
        Class<?>[] args = GenericTypeResolver.resolveTypeArguments(converter.getClass(), genericIfc);
        return (args != null ? new GenericConverter.ConvertiblePair(args[0], args[1]) : null);
    }

    private void invalidateCache() {
        this.converterCache.clear();
    }

    private Object handleConverterNotFound(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            assertNotPrimitiveTargetType(sourceType, targetType);
            return source;
        }
        if (sourceType.isAssignableTo(targetType) && targetType.getObjectType().isInstance(source)) {
            return source;
        }

        try {
            throw new Exception(sourceType + "," + targetType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Object handleResult(TypeDescriptor sourceType, TypeDescriptor targetType, Object result) {
        if (result == null) {
            assertNotPrimitiveTargetType(sourceType, targetType);
        }
        return result;
    }

    private void assertNotPrimitiveTargetType(TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (targetType.isPrimitive()) {
            throw new IllegalArgumentException("A null value cannot be assigned to a primitive type");
        }
    }


    /**
     * Converter -> GenericConverter
     */
    private final class ConverterAdapter implements ConditionalGenericConverter {

        private final Converter<Object, Object> converter;

        private final ConvertiblePair typeInfo;

        public ConverterAdapter(Converter<?, ?> converter, ConvertiblePair typeInfo) {
            this.converter = (Converter<Object, Object>) converter;
            this.typeInfo = typeInfo;
        }

        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(this.typeInfo);
        }

        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (!this.typeInfo.getTargetType().equals(targetType.getObjectType())) {
                return false;
            }
            if (this.converter instanceof ConditionalConverter) {
                return ((ConditionalConverter) this.converter).matches(sourceType, targetType);
            }
            return true;
        }

        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (source == null) {
                return convertNullSource(sourceType, targetType);
            }
            return this.converter.convert(source);
        }

        @Override
        public String toString() {
            return this.typeInfo + " : " + this.converter;
        }
    }


    /**
     * ConverterFactory -> GenericConverter
     */
    private final class ConverterFactoryAdapter implements ConditionalGenericConverter {

        private final ConverterFactory<Object, Object> converterFactory;

        private final ConvertiblePair typeInfo;

        public ConverterFactoryAdapter(ConverterFactory<?, ?> converterFactory, ConvertiblePair typeInfo) {
            this.converterFactory = (ConverterFactory<Object, Object>) converterFactory;
            this.typeInfo = typeInfo;
        }

        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(this.typeInfo);
        }

        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            boolean matches = true;
            if (this.converterFactory instanceof ConditionalConverter) {
                matches = ((ConditionalConverter) this.converterFactory).matches(sourceType, targetType);
            }
            if (matches) {
                Converter<?, ?> converter = this.converterFactory.getConverter(targetType.getType());
                if (converter instanceof ConditionalConverter) {
                    matches = ((ConditionalConverter) converter).matches(sourceType, targetType);
                }
            }
            return matches;
        }

        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (source == null) {
                return convertNullSource(sourceType, targetType);
            }
            return this.converterFactory.getConverter(targetType.getObjectType()).convert(source);
        }

        @Override
        public String toString() {
            return this.typeInfo + " : " + this.converterFactory;
        }
    }

    /**
     * Key for use with the converter cache.
     */
    private static final class ConverterCacheKey {

        private final TypeDescriptor sourceType;

        private final TypeDescriptor targetType;

        public ConverterCacheKey(TypeDescriptor sourceType, TypeDescriptor targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ConverterCacheKey)) {
                return false;
            }
            ConverterCacheKey otherKey = (ConverterCacheKey) other;
            return ObjectUtils.nullSafeEquals(this.sourceType, otherKey.sourceType) &&
                    ObjectUtils.nullSafeEquals(this.targetType, otherKey.targetType);
        }

        @Override
        public int hashCode() {
            return ObjectUtils.nullSafeHashCode(this.sourceType) * 29 +
                    ObjectUtils.nullSafeHashCode(this.targetType);
        }

        @Override
        public String toString() {
            return "ConverterCacheKey [sourceType = " + this.sourceType +
                    ", targetType = " + this.targetType + "]";
        }
    }


    /**
     * Manages all converters registered with the service.
     */
    private static class Converters {

        private final Set<GenericConverter> globalConverters = new LinkedHashSet<>();

        private final Map<ConvertiblePair, ConvertersForPair> converters =                new LinkedHashMap<>(36);

        public void add(GenericConverter converter) {
            Set<ConvertiblePair> convertibleTypes = converter.getConvertibleTypes();
            if (convertibleTypes == null) {
                Assert.state(converter instanceof ConditionalConverter, "Only conditional converters may return null convertible types");
                this.globalConverters.add(converter);
            } else {
                for (ConvertiblePair convertiblePair : convertibleTypes) {
                    ConvertersForPair convertersForPair = getMatchableConverters(convertiblePair);
                    convertersForPair.add(converter);
                }
            }
        }

        private ConvertersForPair getMatchableConverters(ConvertiblePair convertiblePair) {
            ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
            if (convertersForPair == null) {
                convertersForPair = new ConvertersForPair();
                this.converters.put(convertiblePair, convertersForPair);
            }
            return convertersForPair;
        }

        public void remove(Class<?> sourceType, Class<?> targetType) {
            this.converters.remove(new ConvertiblePair(sourceType, targetType));
        }

        public GenericConverter find(TypeDescriptor sourceType, TypeDescriptor targetType) {
            // Search the full type hierarchy
            List<Class<?>> sourceCandidates = getClassHierarchy(sourceType.getType());
            List<Class<?>> targetCandidates = getClassHierarchy(targetType.getType());
            for (Class<?> sourceCandidate : sourceCandidates) {
                for (Class<?> targetCandidate : targetCandidates) {
                    ConvertiblePair convertiblePair = new ConvertiblePair(sourceCandidate, targetCandidate);
                    GenericConverter converter = getRegisteredConverter(sourceType, targetType, convertiblePair);
                    if (converter != null) {
                        return converter;
                    }
                }
            }
            return null;
        }

        private GenericConverter getRegisteredConverter(TypeDescriptor sourceType,
                                                        TypeDescriptor targetType, ConvertiblePair convertiblePair) {

            // Check specifically registered converters
            ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
            if (convertersForPair != null) {
                GenericConverter converter = convertersForPair.getConverter(sourceType, targetType);
                if (converter != null) {
                    return converter;
                }
            }
            // Check ConditionalGenericConverter that match all types
            for (GenericConverter globalConverter : this.globalConverters) {
                if (((ConditionalConverter) globalConverter).matches(sourceType, targetType)) {
                    return globalConverter;
                }
            }
            return null;
        }

        private List<Class<?>> getClassHierarchy(Class<?> type) {
            List<Class<?>> hierarchy = new ArrayList<Class<?>>(20);
            Set<Class<?>> visited = new HashSet<Class<?>>(20);
            addToClassHierarchy(0, ClassUtils.resolvePrimitiveIfNecessary(type), false, hierarchy, visited);
            boolean array = type.isArray();
            int i = 0;
            while (i < hierarchy.size()) {
                Class<?> candidate = hierarchy.get(i);
                candidate = (array ? candidate.getComponentType() : ClassUtils.resolvePrimitiveIfNecessary(candidate));
                Class<?> superclass = candidate.getSuperclass();
                if (candidate.getSuperclass() != null && superclass != Object.class) {
                    addToClassHierarchy(i + 1, candidate.getSuperclass(), array, hierarchy, visited);
                }
                for (Class<?> implementedInterface : candidate.getInterfaces()) {
                    addToClassHierarchy(hierarchy.size(), implementedInterface, array, hierarchy, visited);
                }
                i++;
            }
            addToClassHierarchy(hierarchy.size(), Object.class, array, hierarchy, visited);
            addToClassHierarchy(hierarchy.size(), Object.class, false, hierarchy, visited);
            return hierarchy;
        }

        private void addToClassHierarchy(int index, Class<?> type, boolean asArray,
                                         List<Class<?>> hierarchy, Set<Class<?>> visited) {
            if (asArray) {
                type = Array.newInstance(type, 0).getClass();
            }
            if (visited.add(type)) {
                hierarchy.add(index, type);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ConversionService converters =\n");
            for (String converterString : getConverterStrings()) {
                builder.append('\t').append(converterString).append('\n');
            }
            return builder.toString();
        }

        private List<String> getConverterStrings() {
            List<String> converterStrings = new ArrayList<>();
            for (ConvertersForPair convertersForPair : converters.values()) {
                converterStrings.add(convertersForPair.toString());
            }
            Collections.sort(converterStrings);
            return converterStrings;
        }
    }


    /**
     * Manages converters registered with a specific ConvertiblePair
     */
    private static class ConvertersForPair {

        private final LinkedList<GenericConverter> converters = new LinkedList<>();

        public void add(GenericConverter converter) {
            this.converters.addFirst(converter);
        }

        public GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
            for (GenericConverter converter : this.converters) {

                if (!(converter instanceof ConditionalGenericConverter) ||
                        ((ConditionalGenericConverter) converter).matches(sourceType, targetType)) {
                    return converter;
                }

            }
            return null;
        }

        @Override
        public String toString() {
            return StringUtils.collectionToCommaDelimitedString(this.converters);
        }
    }


    /**
     * 不执行操作的转换器
     */
    private static class NoOpConverter implements GenericConverter {

        private final String name;

        public NoOpConverter(String name) {
            this.name = name;
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return null;
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            return source;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

}
