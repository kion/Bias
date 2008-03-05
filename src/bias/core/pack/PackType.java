//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.03.05 at 02:16:52 AM EET 
//


package bias.core.pack;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for pack-type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="pack-type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AppCore"/>
 *     &lt;enumeration value="Extension"/>
 *     &lt;enumeration value="Skin"/>
 *     &lt;enumeration value="IconSet"/>
 *     &lt;enumeration value="Library"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum PackType {

    @XmlEnumValue("AppCore")
    APP_CORE("AppCore"),
    @XmlEnumValue("Extension")
    EXTENSION("Extension"),
    @XmlEnumValue("Skin")
    SKIN("Skin"),
    @XmlEnumValue("IconSet")
    ICON_SET("IconSet"),
    @XmlEnumValue("Library")
    LIBRARY("Library");
    private final String value;

    PackType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PackType fromValue(String v) {
        for (PackType c: PackType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
