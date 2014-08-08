//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.01.13 at 08:24:23 PM WET 
//

package com.lyncode.xoai.dataprovider.xml.oaipmh;

import com.lyncode.xoai.dataprovider.exceptions.WritingXmlException;
import com.lyncode.xoai.dataprovider.xml.XMLWritable;
import com.lyncode.xoai.dataprovider.xml.XmlOutputContext;

import javax.xml.bind.annotation.*;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

/**
 * A header has a unique identifier, a datestamp, and withSpec(s) in case the
 * item from which the record is disseminated belongs to set(s). the header can
 * carry a deleted status indicating that the record is deleted.
 * <p/>
 * <p/>
 * Java class for headerType complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType name="headerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="identifier" type="{http://www.openarchives.org/OAI/2.0/}identifierType"/>
 *         &lt;element name="datestamp" type="{http://www.openarchives.org/OAI/2.0/}UTCdatetimeType"/>
 *         &lt;element name="withSpec" type="{http://www.openarchives.org/OAI/2.0/}setSpecType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="status" type="{http://www.openarchives.org/OAI/2.0/}statusType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "headerType", propOrder = {"identifier", "datestamp",
        "setSpec"})
public class HeaderType implements XMLWritable {

    @XmlElement(required = true)
    protected String identifier;
    @XmlElement(required = true)
    protected String datestamp;
    protected List<String> setSpec;
    @XmlAttribute(name = "status")
    protected StatusType status;

    /**
     * Gets the value of the identifier property.
     *
     * @return possible object is {@link String }
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     *
     * @param value allowed object is {@link String }
     */
    public void setIdentifier(String value) {
        this.identifier = value;
    }

    /**
     * Gets the value of the datestamp property.
     *
     * @return possible object is {@link String }
     */
    public String getDatestamp() {
        return datestamp;
    }

    /**
     * Sets the value of the datestamp property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDatestamp(String value) {
        this.datestamp = value;
    }

    /**
     * Gets the value of the withSpec property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the withSpec property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getSetSpec().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getSetSpec() {
        if (setSpec == null) {
            setSpec = new ArrayList<String>();
        }
        return this.setSpec;
    }

    /**
     * Gets the value of the status property.
     *
     * @return possible object is {@link StatusType }
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value allowed object is {@link StatusType }
     */
    public void setStatus(StatusType value) {
        this.status = value;
    }

    @Override
    public void write(XmlOutputContext context) throws WritingXmlException {
        try {
            if (this.status != null)
                context.getWriter().writeAttribute("status", this.status.value());

            if (this.identifier != null) {
                context.getWriter().writeStartElement("identifier");
                context.getWriter().writeCharacters(this.identifier);
                context.getWriter().writeEndElement();
            }

            if (this.datestamp != null) {
                context.getWriter().writeStartElement("datestamp");
                context.getWriter().writeCharacters(this.datestamp);
                context.getWriter().writeEndElement();
            }

            for (String setSpec : this.getSetSpec()) {
                context.getWriter().writeStartElement("setSpec");
                context.getWriter().writeCharacters(setSpec);
                context.getWriter().writeEndElement();
            }
        } catch (XMLStreamException e) {
            throw new WritingXmlException(e);
        }
    }

}
