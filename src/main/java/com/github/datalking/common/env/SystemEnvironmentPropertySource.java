package com.github.datalking.common.env;

import com.github.datalking.util.Assert;

import java.util.Map;

/**
 * @author yaoo on 5/29/18
 */
public class SystemEnvironmentPropertySource extends MapPropertySource {

    public SystemEnvironmentPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    @Override
    public boolean containsProperty(String name) {
        return (getProperty(name) != null);
    }

    @Override
    public Object getProperty(String name) {
        String actualName = resolvePropertyName(name);
        if (logger.isDebugEnabled() && !name.equals(actualName)) {
            logger.debug(String.format("PropertySource [%s] does not contain '%s', but found equivalent '%s'",
                    getName(), name, actualName));
        }
        return super.getProperty(actualName);
    }

    /**
     * Check to see if this property source contains a property with the given name, or
     * any underscore / uppercase variation thereof. Return the resolved name if one is
     * found or otherwise the original name. Never returns {@code null}.
     */
    private String resolvePropertyName(String name) {
        Assert.notNull(name, "Property name must not be null");
        if (super.containsProperty(name)) {
            return name;
        }

        String usName = name.replace('.', '_');
        if (!name.equals(usName) && super.containsProperty(usName)) {
            return usName;
        }

        String ucName = name.toUpperCase();
        if (!name.equals(ucName)) {
            if (super.containsProperty(ucName)) {
                return ucName;
            } else {
                String usUcName = ucName.replace('.', '_');
                if (!ucName.equals(usUcName) && super.containsProperty(usUcName)) {
                    return usUcName;
                }
            }
        }

        return name;
    }

}
