//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.01.13 at 08:24:23 PM WET 
//

package com.lyncode.xoai.dataprovider.xml.oaipmh;

import com.lyncode.xoai.dataprovider.xml.XMLWritable;
import com.lyncode.xoai.dataprovider.exceptions.WritingXmlException;
import com.lyncode.xoai.dataprovider.xml.XmlOutputContext;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A record has a header, a metadata part, and an optional about container
 * <p/>
 * <p/>
 * Java class for recordType complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType name="recordType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="header" type="{http://www.openarchives.org/OAI/2.0/}headerType"/>
 *         &lt;element name="metadata" type="{http://www.openarchives.org/OAI/2.0/}metadataType" minOccurs="0"/>
 *         &lt;element name="about" type="{http://www.openarchives.org/OAI/2.0/}aboutType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "recordType", propOrder = {"header", "metadata", "about"})
public class RecordType implements XMLWritable {

    @XmlElement(required = true)
    protected HeaderType header;
    protected MetadataType metadata;
    protected List<AboutType> about;

    /**
     * Gets the value of the header property.
     *
     * @return possible object is {@link HeaderType }
     */
    public HeaderType getHeader() {
        return header;
    }

    /**
     * Sets the value of the header property.
     *
     * @param value allowed object is {@link HeaderType }
     */
    public void setHeader(HeaderType value) {
        this.header = value;
    }

    /**
     * Gets the value of the metadata property.
     *
     * @return possible object is {@link MetadataType }
     */
    public MetadataType getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     *
     * @param value allowed object is {@link MetadataType }
     */
    public void setMetadata(MetadataType value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the about property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the about property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getAbout().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link AboutType }
     */
    public List<AboutType> getAbout() {
        if (about == null) {
            about = new ArrayList<AboutType>();
        }
        return this.about;
    }

    @Override
    public void write(XmlOutputContext context) throws WritingXmlException {
        try {
            if (this.header != null) {
                context.getWriter().writeStartElement("header");
                this.header.write(context);
                context.getWriter().writeEndElement();
            }

            if (this.metadata != null) {
                context.getWriter().writeStartElement("metadata");
                this.metadata.write(context);
                context.getWriter().writeEndElement();
            }

            for (int i = 0; i < this.getAbout().size(); i++) {
                context.getWriter().writeStartElement("about");
                this.getAbout().get(i).write(context);
                context.getWriter().writeEndElement();
            }
        } catch (XMLStreamException e) {
            throw new WritingXmlException(e);
        }
    }

}
