
package org.onap.ccsdk.sli.core.sli;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurePrinter {
    private static final Logger LOG = LoggerFactory.getLogger(SecurePrinter.class);
    private static final String DEFAULT_FILTER = "password,pass,pswd";
    private static final String REDACTED = "***REDACTED***";
    private static final String FILTER_PROPERTY = "NODE_STRING_FILTER";
    private static final String SEPERATOR = " = ";
    private static final String COMMON_ERROR_MESSAGE = "Failed to print properties";

    private static String[] filterArray;

    public SecurePrinter() {
        String filterProperty = System.getProperty(FILTER_PROPERTY);
        if (filterProperty != null && !filterProperty.isEmpty() && filterProperty.contains(",")) {
            filterArray = filterProperty.split(",");
        } else {
            filterArray = DEFAULT_FILTER.split(",");
        }
    }

    private String filterValue(String key, String value) {
        String normalizedKey = key.toLowerCase();
        for (String restrictedKey : filterArray) {
            if (normalizedKey.contains(restrictedKey)) {
                return REDACTED;
            }
        }
        return value;
    }

    public void printAttributes(HashMap<String, String> attributes) {
        if (LOG.isDebugEnabled()) {
            for (Entry<String, String> attribute : attributes.entrySet()) {
                String value = filterValue(attribute.getKey(), attribute.getValue());
                LOG.debug(attribute.getKey() + SEPERATOR + value);
            }
        }
    }

    public void printAttributes(HashMap<String, String> attributes, String subpath) {
        if (LOG.isDebugEnabled()) {
            for (Entry<String, String> attribute : attributes.entrySet()) {
                if (attribute.getKey().startsWith(subpath)) {
                    String value = filterValue(attribute.getKey(), attribute.getValue());
                    LOG.debug(attribute.getKey() + SEPERATOR + value);
                }
            }
        }
    }

    public void printProperties(Properties props) {
        if (LOG.isDebugEnabled()) {
            try {
                for (Entry<Object, Object> property : props.entrySet()) {
                    String keyString = (String) property.getKey();
                    String valueString = (String) property.getValue();
                    String value = filterValue(keyString, valueString);
                    LOG.debug(keyString + SEPERATOR + value);
                }
            } catch (Exception e) {
                LOG.error(COMMON_ERROR_MESSAGE, e);
            }
        }
    }

    public void printProperties(Properties props, String subpath) {       
        if (LOG.isDebugEnabled()) {
            try {
                for (Entry<Object, Object> property : props.entrySet()) {
                    String keyString = (String) property.getKey();
                    if (keyString.startsWith(subpath)) {
                        String valueString = (String) property.getValue();
                        String value = filterValue(keyString, valueString);
                        LOG.debug(keyString + SEPERATOR + value);
                    }
                }
            } catch (Exception e) {
                LOG.error(COMMON_ERROR_MESSAGE, e);
            }
        }
    }
    
    public void printPropertiesAlphabetically(Properties props) {
        if (LOG.isDebugEnabled()) {           
            TreeMap<String,String> sortedMap = new TreeMap(props);
            try {
                for (Entry<String, String> entry : sortedMap.entrySet()) {
                    String keyString = (String) entry.getKey();
                    String valueString = (String) entry.getValue();
                    String value = filterValue(keyString, valueString);
                    LOG.debug(keyString + SEPERATOR + value);
                }
            } catch (Exception e) {
                LOG.error(COMMON_ERROR_MESSAGE, e);
            }
        }
    }
    
}