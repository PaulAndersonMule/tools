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
 * @author panderson
 */
@Entity
@Table(name = "LIBRARY_SECTION", catalog = "", schema = "")
@XmlRootElement
@NamedQueries({
  @NamedQuery(name = "XPathLibrarySection.findAll", query = "SELECT l FROM XPathLibrarySection l"),
  @NamedQuery(name = "XPathLibrarySection.findBySectionName", query = "SELECT l FROM XPathLibrarySection l WHERE l.sectionName = :sectionName")})
public class XPathLibrarySection implements Serializable, Comparable<XPathLibrarySection> {

  private static final long serialVersionUID = 1L;
  @Id
  @Basic(optional = false)
  @Column(name = "SECTION_NAME", nullable = false, length = 20)
  private String sectionName;
  @OneToMany(orphanRemoval = true, mappedBy = "xpathLibrarySection1", cascade = {CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER)
  private List<XPathItem> xpathItemList;

  public XPathLibrarySection() {
  }

  public XPathLibrarySection(String sectionName) {
    this.sectionName = sectionName;
  }

  public String getSectionName() {
    return sectionName;
  }

  public void setSectionName(String sectionName) {
    this.sectionName = sectionName;
  }

  @XmlTransient
  public List<XPathItem> getXpathLibraryList() {
    return xpathItemList;
  }

  public void setXpathItemList(List<XPathItem> xpathItemList) {
    this.xpathItemList = xpathItemList;
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
    if (!(object instanceof XPathLibrarySection)) {
      return false;
    }
    XPathLibrarySection other = (XPathLibrarySection) object;
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
  public int compareTo(XPathLibrarySection o) {
    return getSectionName().compareTo(o.getSectionName());
  }

}
