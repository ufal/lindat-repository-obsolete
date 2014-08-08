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

import javax.xml.bind.annotation.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.lyncode.xoai.util.XmlIOUtils.writeValue;

/**
 * <p/>
 * Java class for metadataFormatType complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType name="metadataFormatType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="metadataPrefix" type="{http://www.openarchives.org/OAI/2.0/}metadataPrefixType"/>
 *         &lt;element name="schema" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="metadataNamespace" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "metadataFormatType", propOrder = {"metadataPrefix", "schema",
        "metadataNamespace"})
public class MetadataFormatType implements XMLWritable {

    @XmlElement(required = true)
    protected String metadataPrefix;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String schema;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String metadataNamespace;

    /**
     * Gets the value of the metadataPrefix property.
     *
     * @return possible object is {@link String }
     */
    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    /**
     * Sets the value of the metadataPrefix property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMetadataPrefix(String value) {
        this.metadataPrefix = value;
    }

    /**
     * Gets the value of the schema property.
     *
     * @return possible object is {@link String }
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the value of the schema property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSchema(String value) {
        this.schema = value;
    }

    /**
     * Gets the value of the metadataNamespace property.
     *
     * @return possible object is {@link String }
     */
    public String getMetadataNamespace() {
        return metadataNamespace;
    }

    /**
     * Sets the value of the metadataNamespace property.
     *
     * @param value allowed object is {@link String }
     */
    public void setMetadataNamespace(String value) {
        this.metadataNamespace = value;
    }

    /*
     *
 *         &lt;element name="metadataPrefix" type="{http://www.openarchives.org/OAI/2.0/}metadataPrefixType"/>
 *         &lt;element name="schema" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="metadataNamespace" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
     */
    @Override
    public void write(XmlOutputContext context) throws WritingXmlException {
        try {
            if (metadataPrefix != null)
                writeValue(context.getWriter(), "metadataPrefix", metadataPrefix);
            if (schema != null)
                writeValue(context.getWriter(), "schema", schema);
            if (metadataNamespace != null)
                writeValue(context.getWriter(), "metadataNamespace", metadataNamespace);
        } catch (XMLStreamException e) {
            throw new WritingXmlException(e);
        }
    }

}
