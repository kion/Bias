//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.02.01 at 07:57:56 PM GMT 
//


package bias.extension.FinancialFlows.xmlb;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="single" type="{http://bias.sourceforge.net}singleFlows"/>
 *         &lt;element name="regular" type="{http://bias.sourceforge.net}regularFlows"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "single",
    "regular"
})
@XmlRootElement(name = "parts")
public class Parts {

    protected SingleFlows single;
    protected RegularFlows regular;

    /**
     * Gets the value of the single property.
     * 
     * @return
     *     possible object is
     *     {@link SingleFlows }
     *     
     */
    public SingleFlows getSingle() {
        return single;
    }

    /**
     * Sets the value of the single property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleFlows }
     *     
     */
    public void setSingle(SingleFlows value) {
        this.single = value;
    }

    /**
     * Gets the value of the regular property.
     * 
     * @return
     *     possible object is
     *     {@link RegularFlows }
     *     
     */
    public RegularFlows getRegular() {
        return regular;
    }

    /**
     * Sets the value of the regular property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegularFlows }
     *     
     */
    public void setRegular(RegularFlows value) {
        this.regular = value;
    }

}
