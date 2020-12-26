/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pa.xpath.data;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author paul.anderson
 */
@Embeddable
public class XSLTItemPK implements Serializable {

	@Basic(optional = false)
  @Column(name = "XSLT_LIBRARY_SECTION", nullable = false, length = 20)
	private String xsltLibrarySection;
	@Basic(optional = false)
  @Column(name = "ITEM_NAME", nullable = false, length = 40)
	private String itemName;

	public XSLTItemPK() {
	}

	public XSLTItemPK(String xsltLibrarySection, String itemName) {
		this.xsltLibrarySection = xsltLibrarySection;
		this.itemName = itemName;
	}

	public String getXsltLibrarySection() {
		return xsltLibrarySection;
	}

	public void setXsltLibrarySection(String xsltLibrarySection) {
		this.xsltLibrarySection = xsltLibrarySection;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (xsltLibrarySection != null ? xsltLibrarySection.hashCode() : 0);
		hash += (itemName != null ? itemName.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof XSLTItemPK)) {
			return false;
		}
		XSLTItemPK other = (XSLTItemPK) object;
		if ((this.xsltLibrarySection == null && other.xsltLibrarySection != null) || (this.xsltLibrarySection != null && !this.xsltLibrarySection.equals(other.xsltLibrarySection))) {
			return false;
		}
		if ((this.itemName == null && other.itemName != null) || (this.itemName != null && !this.itemName.equals(other.itemName))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.pa.xpath.data.XSLTLibraryPK[ xsltLibrarySection=" + xsltLibrarySection + ", itemName=" + itemName + " ]";
	}
	
}
