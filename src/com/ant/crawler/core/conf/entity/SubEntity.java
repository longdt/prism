//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.08 at 09:28:54 AM ICT 
//


package com.ant.crawler.core.conf.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for subEntity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="subEntity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="scriptSite" type="{}scriptSite" minOccurs="0"/>
 *         &lt;element name="listSite" type="{}listSite" minOccurs="0"/>
 *         &lt;element name="detailSite" type="{}detailSite"/>
 *       &lt;/sequence>
 *       &lt;attribute name="link" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "subEntity", propOrder = {
    "scriptSite",
    "listSite",
    "detailSite"
})
public class SubEntity {

    protected ScriptSite scriptSite;
    protected ListSite listSite;
    @XmlElement(required = true)
    protected DetailSite detailSite;
    @XmlAttribute(name = "link", required = true)
    protected String link;

    /**
     * Gets the value of the scriptSite property.
     * 
     * @return
     *     possible object is
     *     {@link ScriptSite }
     *     
     */
    public ScriptSite getScriptSite() {
        return scriptSite;
    }

    /**
     * Sets the value of the scriptSite property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScriptSite }
     *     
     */
    public void setScriptSite(ScriptSite value) {
        this.scriptSite = value;
    }

    /**
     * Gets the value of the listSite property.
     * 
     * @return
     *     possible object is
     *     {@link ListSite }
     *     
     */
    public ListSite getListSite() {
        return listSite;
    }

    /**
     * Sets the value of the listSite property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListSite }
     *     
     */
    public void setListSite(ListSite value) {
        this.listSite = value;
    }

    /**
     * Gets the value of the detailSite property.
     * 
     * @return
     *     possible object is
     *     {@link DetailSite }
     *     
     */
    public DetailSite getDetailSite() {
        return detailSite;
    }

    /**
     * Sets the value of the detailSite property.
     * 
     * @param value
     *     allowed object is
     *     {@link DetailSite }
     *     
     */
    public void setDetailSite(DetailSite value) {
        this.detailSite = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(String value) {
        this.link = value;
    }

}
