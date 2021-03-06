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
import java.util.Date;

/**
 * Define requestType, indicating the protocol request that led to the response.
 * Element content is BASE-URL, attributes are arguments of protocol request,
 * attribute-values are values of arguments of protocol request
 * <p/>
 * <p/>
 * Java class for requestType complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p/>
 * <pre>
 * &lt;complexType name="requestType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anyURI">
 *       &lt;attribute name="verb" type="{http://www.openarchives.org/OAI/2.0/}verbType" />
 *       &lt;attribute name="identifier" type="{http://www.openarchives.org/OAI/2.0/}identifierType" />
 *       &lt;attribute name="metadataPrefix" type="{http://www.openarchives.org/OAI/2.0/}metadataPrefixType" />
 *       &lt;attribute name="from" type="{http://www.openarchives.org/OAI/2.0/}UTCdatetimeType" />
 *       &lt;attribute name="until" type="{http://www.openarchives.org/OAI/2.0/}UTCdatetimeType" />
 *       &lt;attribute name="set" type="{http://www.openarchives.org/OAI/2.0/}setSpecType" />
 *       &lt;attribute name="resumptionToken" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "requestType", propOrder = {"value"})
public class RequestType implements XMLWritable {

    @XmlValue
    @XmlSchemaType(name = "anyURI")
    protected String value;
    @XmlAttribute(name = "verb")
    protected VerbType verb;
    @XmlAttribute(name = "identifier")
    protected String identifier;
    @XmlAttribute(name = "metadataPrefix")
    protected String metadataPrefix;
    @XmlAttribute(name = "from")
    protected Date from;
    @XmlAttribute(name = "until")
    protected Date until;
    @XmlAttribute(name = "set")
    protected String set;
    @XmlAttribute(name = "resumptionToken")
    protected String resumptionToken;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the verb property.
     *
     * @return possible object is {@link VerbType }
     */
    public VerbType getVerb() {
        return verb;
    }

    /**
     * Sets the value of the verb property.
     *
     * @param value allowed object is {@link VerbType }
     */
    public void setVerb(VerbType value) {
        this.verb = value;
    }

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
     * Gets the value of the from property.
     *
     * @return possible object is {@link String }
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     *
     * @param value allowed object is {@link String }
     */
    public void setFrom(Date value) {
        this.from = value;
    }

    /**
     * Gets the value of the until property.
     *
     * @return possible object is {@link String }
     */
    public Date getUntil() {
        return until;
    }

    /**
     * Sets the value of the until property.
     *
     * @param value allowed object is {@link String }
     */
    public void setUntil(Date value) {
        this.until = value;
    }

    /**
     * Gets the value of the set property.
     *
     * @return possible object is {@link String }
     */
    public String getSet() {
        return set;
    }

    /**
     * Sets the value of the set property.
     *
     * @param value allowed object is {@link String }
     */
    public void setSet(String value) {
        this.set = value;
    }

    /**
     * Gets the value of the resumptionToken property.
     *
     * @return possible object is {@link String }
     */
    public String getResumptionToken() {
        return resumptionToken;
    }

    /**
     * Sets the value of the resumptionToken property.
     *
     * @param value allowed object is {@link String }
     */
    public void setResumptionToken(String value) {
        this.resumptionToken = value;
    }

    /**
     * &lt;attribute name="verb" type="{http://www.openarchives.org/OAI/2.0/}verbType" />
     * &lt;attribute name="identifier" type="{http://www.openarchives.org/OAI/2.0/}identifierType" />
     * &lt;attribute name="metadataPrefix" type="{http://www.openarchives.org/OAI/2.0/}metadataPrefixType" />
     * &lt;attribute name="from" type="{http://www.openarchives.org/OAI/2.0/}UTCdatetimeType" />
     * &lt;attribute name="until" type="{http://www.openarchives.org/OAI/2.0/}UTCdatetimeType" />
     * &lt;attribute name="set" type="{http://www.openarchives.org/OAI/2.0/}setSpecType" />
     * &lt;attribute name="resumptionToken" type="{http://www.w3.org/2001/XMLSchema}string" />
     */
    @Override
    public void write(XmlOutputContext context) throws WritingXmlException {
        try {
            if (verb != null)
                context.getWriter().writeAttribute("verb", verb.value());
            if (identifier != null)
                context.getWriter().writeAttribute("identifier", identifier);
            if (metadataPrefix != null)
                context.getWriter().writeAttribute("metadataPrefix", metadataPrefix);
            if (from != null)
                context.getWriter().writeAttribute("from", context.format(from));
            if (until != null)
                context.getWriter().writeAttribute("until", context.format(until));
            if (set != null)
                context.getWriter().writeAttribute("set", set);
            if (resumptionToken != null)
                context.getWriter().writeAttribute("resumptionToken", resumptionToken);

            if (value != null)
                context.getWriter().writeCharacters(value);
        } catch (XMLStreamException e) {
            throw new WritingXmlException(e);
        }
    }

}
