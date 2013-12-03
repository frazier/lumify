package com.altamiracorp.lumify.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Exposes utility methods for interacting with a {@link Properties} object
 */
public final class PropertyUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtils.class.getName());

    /**
     * Attempts to add the specified property key,value combination to the provided {@link Properties} instance
     * @param props The properties instance used for storage
     * @param property The name of the property to set, not null or empty
     * @param value The value of the property, not null
     * @return True for successful addition, False otherwise
     */
    public static boolean setPropertyValue(final Properties props, final String property, final String value) {
        checkNotNull(props);
        checkNotNull(property);
        checkArgument(!property.isEmpty());
        checkNotNull(value, "Could not store invalid value for property: " + property);

        boolean propertyAdded = false;
        final Object keyValue = props.setProperty(property, value);

        if( keyValue == null ) {
            LOGGER.debug(String.format("Set property: %s=%s", property, value));
            propertyAdded = true;
        }

        return propertyAdded;
    }
}