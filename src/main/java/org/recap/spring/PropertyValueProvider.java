package org.recap.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by sheiks on 23/01/17.
 */
@Component
public class PropertyValueProvider {

    @Autowired
    private Environment env;

    /**
     * Get the value fo the given property key .
     *
     * @param key the key
     * @return the property value
     */
    public String getProperty(String key) {
        return env.getProperty(key);
    }
}
