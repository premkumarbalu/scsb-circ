package org.recap.converter;

import java.util.Map;

/**
 * Created by premkb on 15/12/16.
 */
@FunctionalInterface
public interface XmlToBibEntityConverterInterface {

    /**
     * Convert map.
     *
     * @param record the record
     * @return the map
     */
    public Map convert(Object record);
}
