/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pa.xpath.data;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author paul.anderson
 */
@Entity
@Table(name = "XSLT_LIBRARY", catalog = "", schema = "")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name = "XSLTItem.findAll", query = "SELECT x FROM XSLTItem x"),
	@NamedQuery(name = "XSLTItem.findByXSLTLibrarySection", query = "SELECT x FROM XSLTItem x WHERE x.xsltLibrarySection.sectionName = :xsltLibrarySection"),
	@NamedQuery(name = "XSLTItem.findByItemName", query = "SELECT x FROM XSLTItem x WHERE x.xsltItemPK.itemName = :itemName")})
public class XSLTItem implements Serializable,Comparable<XSLTItem> {

	private static final long serialVersionUID = 1L;
	@EmbeddedId
	protected XSLTItemPK xsltItemPK;
	@Basic(optional = false)
  @Lob
  @Column(name = "XSLT", nullable = false)
	private String xslt;
	@JoinColumn(name = "XSLT_LIBRARY_SECTION", referencedColumnName = "SECTION_NAME", nullable = false, insertable = false, updatable = false)
  @ManyToOne(optional = false, cascade = CascadeType.ALL)
	private XSLTLibrarySection xsltLibrarySection;

	public XSLTItem() {
	}

	public XSLTItem(XSLTItemPK xSLTLibraryPK) {
		this.xsltItemPK = xSLTLibraryPK;
	}

	public XSLTItem(XSLTItemPK xSLTLibraryPK, String xslt) {
		this.xsltItemPK = xSLTLibraryPK;
		this.xslt = xslt;
	}

	public XSLTItem(String xsltLibrarySection, String itemName) {
		this.xsltItemPK = new XSLTItemPK(xsltLibrarySection, itemName);
	}

	public XSLTItemPK getXSLTLibraryPK() {
		return xsltItemPK;
	}

	public void setXSLTLibraryPK(XSLTItemPK xSLTLibraryPK) {
		this.xsltItemPK = xSLTLibraryPK;
	}

	public String getXSLT() {
		return xslt;
	}

	public void setXSLT(String xslt) {
		this.xslt = xslt;
	}

	public XSLTLibrarySection getXSLTLibrarySection() {
		return xsltLibrarySection;
	}

	public void setXSLTLibrarySection(XSLTLibrarySection xSLTLibrarySection) {
		this.xsltLibrarySection = xSLTLibrarySection;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (xsltItemPK != null ? xsltItemPK.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof XSLTItem)) {
			return false;
		}
		XSLTItem other = (XSLTItem) object;
		if ((this.xsltItemPK == null && other.xsltItemPK != null) || (this.xsltItemPK != null && !this.xsltItemPK.equals(other.xsltItemPK))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return xsltItemPK.getItemName();
	}

	@Override
	public int compareTo(XSLTItem o) {
		if (this.xsltItemPK.getItemName() != null && o.getXSLTLibraryPK().getItemName() != null){
			return this.xsltItemPK.getItemName().compareTo(o.getXSLTLibraryPK().getItemName());
		}
		return 0;
	}
	
}
