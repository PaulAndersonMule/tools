package com.pa.xpath.data;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author panderson
 */
@Entity
@Table(name = "XPATH_LIBRARY", catalog = "", schema = "")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "XPathItem.findAll", query = "SELECT x FROM XPathItem x"),
    @NamedQuery(name = "XPathItem.findByLibrarySection", query = "SELECT x FROM XPathItem x WHERE x.xpathItemPK.librarySection = :librarySection"),
    @NamedQuery(name = "XPathItem.findByItemName", query = "SELECT x FROM XPathItem x WHERE x.xpathItemPK.itemName = :itemName"),
})
public class XPathItem implements Serializable, Comparable<XPathItem>{
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected XPathItemPK xpathItemPK;
    @Basic(optional = false)
    @Column(name = "XPATH", nullable = false, length = 1000)
    private String xpath;
    @JoinColumn(name = "LIBRARY_SECTION", referencedColumnName = "SECTION_NAME", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    private XPathLibrarySection xpathLibrarySection1;

    public XPathItem() {
    }

    public XPathItem(XPathItemPK xpathLibraryPK) {
        this.xpathItemPK = xpathLibraryPK;
    }

    public XPathItem(XPathItemPK xpathLibraryPK, String xpath) {
        this.xpathItemPK = xpathLibraryPK;
        this.xpath = xpath;
    }

    public XPathItem(String librarySection, String itemName) {
        this.xpathItemPK = new XPathItemPK(librarySection, itemName);
    }

    public XPathItemPK getXpathLibraryPK() {
        return xpathItemPK;
    }

    public void setXpathLibraryPK(XPathItemPK xpathLibraryPK) {
        this.xpathItemPK = xpathLibraryPK;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public XPathLibrarySection getLibrarySection1() {
        return xpathLibrarySection1;
    }

    public void setLibrarySection1(XPathLibrarySection librarySection1) {
        this.xpathLibrarySection1 = librarySection1;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (xpathItemPK != null ? xpathItemPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof XPathItem)) {
            return false;
        }
        XPathItem other = (XPathItem) object;
        if ((this.xpathItemPK == null && other.xpathItemPK != null) || (this.xpathItemPK != null && !this.xpathItemPK.equals(other.xpathItemPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return xpathItemPK.getItemName();
    }

  @Override
  public int compareTo(XPathItem o) {
    return getXpathLibraryPK().getItemName().compareTo(o.getXpathLibraryPK().getItemName());
  }
    
}
