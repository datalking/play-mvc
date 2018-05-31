package com.github.datalking.common.env;

import com.github.datalking.common.ConfigurableConversionService;
import com.github.datalking.util.Assert;
import com.github.datalking.util.ObjectUtils;
import com.github.datalking.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessControlException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.github.datalking.util.StringUtils.commaDelimitedListToStringArray;
import static com.github.datalking.util.StringUtils.trimAllWhitespace;
import static java.lang.String.format;

/**
 * @author yaoo on 5/28/18
 */
public abstract class AbstractEnvironment implements ConfigurableEnvironment {

    public static final String IGNORE_GETENV_PROPERTY_NAME = "spring.getenv.ignore";

    public static final String ACTIVE_PROFILES_PROPERTY_NAME = "spring.profiles.active";

    public static final String DEFAULT_PROFILES_PROPERTY_NAME = "spring.profiles.default";

    protected static final String RESERVED_DEFAULT_PROFILE_NAME = "default";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<String> activeProfiles = new LinkedHashSet<>();

    private final Set<String> defaultProfiles = new LinkedHashSet<>(getReservedDefaultProfiles());

    private final MutablePropertySources propertySources = new MutablePropertySources();
    /**
     * 默认的属性占位符解析器
     */
    private final ConfigurablePropertyResolver propertyResolver = new PropertySourcesPropertyResolver(this.propertySources);

    public AbstractEnvironment() {
        customizePropertySources(this.propertySources);
    }

    protected void customizePropertySources(MutablePropertySources propertySources) {
    }

    protected Set<String> getReservedDefaultProfiles() {
        return Collections.singleton(RESERVED_DEFAULT_PROFILE_NAME);
    }


    //---------------------------------------------------------------------
    // Implementation of ConfigurableEnvironment interface
    //---------------------------------------------------------------------

    public String[] getActiveProfiles() {
        return StringUtils.toStringArray(doGetActiveProfiles());
    }

    protected Set<String> doGetActiveProfiles() {
        synchronized (this.activeProfiles) {
            if (this.activeProfiles.isEmpty()) {
                String profiles = getProperty(ACTIVE_PROFILES_PROPERTY_NAME);
                if (StringUtils.hasText(profiles)) {
                    setActiveProfiles(commaDelimitedListToStringArray(trimAllWhitespace(profiles)));
                }
            }
            return this.activeProfiles;
        }
    }

    public void setActiveProfiles(String... profiles) {
        Assert.notNull(profiles, "Profile array must not be null");
        synchronized (this.activeProfiles) {
            this.activeProfiles.clear();
            for (String profile : profiles) {
                validateProfile(profile);
                this.activeProfiles.add(profile);
            }
        }
    }

    public void addActiveProfile(String profile) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(format("Activating profile '%s'", profile));
        }
        validateProfile(profile);
        doGetActiveProfiles();
        synchronized (this.activeProfiles) {
            this.activeProfiles.add(profile);
        }
    }


    public String[] getDefaultProfiles() {
        return StringUtils.toStringArray(doGetDefaultProfiles());
    }

    protected Set<String> doGetDefaultProfiles() {
        synchronized (this.defaultProfiles) {
            if (this.defaultProfiles.equals(getReservedDefaultProfiles())) {
                String profiles = getProperty(DEFAULT_PROFILES_PROPERTY_NAME);
                if (StringUtils.hasText(profiles)) {
                    setDefaultProfiles(commaDelimitedListToStringArray(trimAllWhitespace(profiles)));
                }
            }
            return this.defaultProfiles;
        }
    }

    public void setDefaultProfiles(String... profiles) {
        Assert.notNull(profiles, "Profile array must not be null");
        synchronized (this.defaultProfiles) {
            this.defaultProfiles.clear();
            for (String profile : profiles) {
                validateProfile(profile);
                this.defaultProfiles.add(profile);
            }
        }
    }

    public boolean acceptsProfiles(String... profiles) {
        Assert.notEmpty(profiles, "Must specify at least one profile");
        for (String profile : profiles) {
            if (StringUtils.hasLength(profile) && profile.charAt(0) == '!') {
                if (!isProfileActive(profile.substring(1))) {
                    return true;
                }
            } else if (isProfileActive(profile)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isProfileActive(String profile) {
        validateProfile(profile);
        Set<String> currentActiveProfiles = doGetActiveProfiles();
        return (currentActiveProfiles.contains(profile) ||
                (currentActiveProfiles.isEmpty() && doGetDefaultProfiles().contains(profile)));
    }

    protected void validateProfile(String profile) {
        if (!StringUtils.hasText(profile)) {
            throw new IllegalArgumentException("Invalid profile [" + profile + "]: must contain text");
        }
        if (profile.charAt(0) == '!') {
            throw new IllegalArgumentException("Invalid profile [" + profile + "]: must not begin with ! operator");
        }
    }

    public MutablePropertySources getPropertySources() {
        return this.propertySources;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSystemEnvironment() {
        if (suppressGetenvAccess()) {
            return Collections.emptyMap();
        }
        try {
            return (Map) System.getenv();
        } catch (AccessControlException ex) {

//            return (Map) new ReadOnlySystemAttributesMap() {
//                @Override
//                protected String getSystemAttribute(String attributeName) {
//                    try {
//                        return System.getenv(attributeName);
//                    } catch (AccessControlException ex) {
//                        if (logger.isInfoEnabled()) {
//                            logger.info(format("Caught AccessControlException when accessing system " +
//                                            "environment variable [%s]; its value will be returned [null]. Reason: %s",
//                                    attributeName, ex.getMessage()));
//                        }
//                        return null;
//                    }
//                }
//            };
            return null;
        }
    }

    protected boolean suppressGetenvAccess() {
//        return SpringProperties.getFlag(IGNORE_GETENV_PROPERTY_NAME);
        return false;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSystemProperties() {
        try {
            return (Map) System.getProperties();
        } catch (AccessControlException ex) {
//            return (Map) new ReadOnlySystemAttributesMap() {
//                @Override
//                protected String getSystemAttribute(String attributeName) {
//                    try {
//                        return System.getProperty(attributeName);
//                    } catch (AccessControlException ex) {
//                        if (logger.isInfoEnabled()) {
//                            logger.info(format("Caught AccessControlException when accessing system " +
//                                            "property [%s]; its value will be returned [null]. Reason: %s",
//                                    attributeName, ex.getMessage()));
//                        }
//                        return null;
//                    }
//                }
//            };
            return null;
        }
    }

    public void merge(ConfigurableEnvironment parent) {

//        for (PropertySource<?> ps : parent.getPropertySources()) {
//            if (!this.propertySources.contains(ps.getName())) {
//                this.propertySources.addLast(ps);
//            }
//        }

        String[] parentActiveProfiles = parent.getActiveProfiles();
        if (!ObjectUtils.isEmpty(parentActiveProfiles)) {
            synchronized (this.activeProfiles) {
                for (String profile : parentActiveProfiles) {
                    this.activeProfiles.add(profile);
                }
            }
        }
        String[] parentDefaultProfiles = parent.getDefaultProfiles();
        if (!ObjectUtils.isEmpty(parentDefaultProfiles)) {
            synchronized (this.defaultProfiles) {
                this.defaultProfiles.remove(RESERVED_DEFAULT_PROFILE_NAME);
                for (String profile : parentDefaultProfiles) {
                    this.defaultProfiles.add(profile);
                }
            }
        }
    }


    //---------------------------------------------------------------------
    // Implementation of ConfigurablePropertyResolver interface
    //---------------------------------------------------------------------

    public ConfigurableConversionService getConversionService() {
        return this.propertyResolver.getConversionService();
    }

    public void setConversionService(ConfigurableConversionService conversionService) {
        this.propertyResolver.setConversionService(conversionService);
    }

    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.propertyResolver.setPlaceholderPrefix(placeholderPrefix);
    }

    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.propertyResolver.setPlaceholderSuffix(placeholderSuffix);
    }

    public void setValueSeparator(String valueSeparator) {
        this.propertyResolver.setValueSeparator(valueSeparator);
    }

    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        this.propertyResolver.setIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders);
    }

    public void setRequiredProperties(String... requiredProperties) {
        this.propertyResolver.setRequiredProperties(requiredProperties);
    }

    public void validateRequiredProperties() {
        this.propertyResolver.validateRequiredProperties();
    }


    //---------------------------------------------------------------------
    // Implementation of PropertyResolver interface
    //---------------------------------------------------------------------

    @Override
    public boolean containsProperty(String key) {
        return this.propertyResolver.containsProperty(key);
    }

    public String getProperty(String key) {
        return this.propertyResolver.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return this.propertyResolver.getProperty(key, defaultValue);
    }

    public <T> T getProperty(String key, Class<T> targetType) {
        return this.propertyResolver.getProperty(key, targetType);
    }

    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return this.propertyResolver.getProperty(key, targetType, defaultValue);
    }

    public <T> Class<T> getPropertyAsClass(String key, Class<T> targetType) {
        return this.propertyResolver.getPropertyAsClass(key, targetType);
    }

    public String getRequiredProperty(String key) throws IllegalStateException {
        return this.propertyResolver.getRequiredProperty(key);
    }

    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return this.propertyResolver.getRequiredProperty(key, targetType);
    }

    public String resolvePlaceholders(String text) {
        return this.propertyResolver.resolvePlaceholders(text);
    }

    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return this.propertyResolver.resolveRequiredPlaceholders(text);
    }

    @Override
    public String toString() {
        return format("%s {activeProfiles=%s, defaultProfiles=%s, propertySources=%s}",
                getClass().getSimpleName(), this.activeProfiles, this.defaultProfiles,
                this.propertySources);
    }

}
