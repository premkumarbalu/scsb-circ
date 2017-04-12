
package org.recap.model.jaxb.marc;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;


/**
 * <p>Java class for recordTypeType.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;simpleType name="recordTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="Bibliographic"/>
 *     &lt;enumeration value="Authority"/>
 *     &lt;enumeration value="Holdings"/>
 *     &lt;enumeration value="Classification"/>
 *     &lt;enumeration value="Community"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "recordTypeType")
@XmlEnum
public enum RecordTypeType implements Serializable {

    /**
     * Bibliographic record type type.
     */
    @XmlEnumValue("Bibliographic")
    BIBLIOGRAPHIC("Bibliographic"),
    /**
     * Authority record type type.
     */
    @XmlEnumValue("Authority")
    AUTHORITY("Authority"),
    /**
     * Holdings record type type.
     */
    @XmlEnumValue("Holdings")
    HOLDINGS("Holdings"),
    /**
     * Classification record type type.
     */
    @XmlEnumValue("Classification")
    CLASSIFICATION("Classification"),
    /**
     * Community record type type.
     */
    @XmlEnumValue("Community")
    COMMUNITY("Community");
    private final String value;

    RecordTypeType(String v) {
        value = v;
    }

    /**
     * Value string.
     *
     * @return the string
     */
    public String value() {
        return value;
    }

    /**
     * From value record type type.
     *
     * @param v the v
     * @return the record type type
     */
    public static RecordTypeType fromValue(String v) {
        for (RecordTypeType c : RecordTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
