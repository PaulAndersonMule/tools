package com.pa.xpath.data;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author paul.anderson
 */
@Entity
@Table(name = "XSLT_LIBRARY_SECTION", catalog = "", schema = "")
@XmlRootElement
@NamedQueries({
	@NamedQuery(name = "XSLTLibrarySection.findAll", query = "SELECT x FROM XSLTLibrarySection x"),
	@NamedQuery(name = "XSLTLibrarySection.findBySectionName", query = "SELECT x FROM XSLTLibrarySection x WHERE x.sectionName = :sectionName")})
public class XSLTLibrarySection implements Serializable, Comparable<XSLTLibrarySection> {

	private static final long serialVersionUID = 1L;
	@Id
  @Basic(optional = false)
  @Column(name = "SECTION_NAME", nullable = false, length = 20)
	private String sectionName;
	@OneToMany(mappedBy = "xsltLibrarySection", cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER)
	private List<XSLTItem> xsltLibraryList;

	public XSLTLibrarySection() {
	}

	public XSLTLibrarySection(String sectionName) {
		this.sectionName = sectionName;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	@XmlTransient
	public List<XSLTItem> getXSLTLibraryList() {
		return xsltLibraryList;
	}

	public void setXSLTLibraryList(List<XSLTItem> xSLTLibraryList) {
		this.xsltLibraryList = xSLTLibraryList;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (sectionName != null ? sectionName.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof XSLTLibrarySection)) {
			return false;
		}
		XSLTLibrarySection other = (XSLTLibrarySection) object;
		if ((this.sectionName == null && other.sectionName != null) || (this.sectionName != null && !this.sectionName.equals(other.sectionName))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return sectionName;
	}

	@Override
	public int compareTo(XSLTLibrarySection o) {
		if (this.sectionName != null && o.getSectionName() != null){
			return this.sectionName.compareTo(o.getSectionName());
		}
		return 0;
	}

	
}
