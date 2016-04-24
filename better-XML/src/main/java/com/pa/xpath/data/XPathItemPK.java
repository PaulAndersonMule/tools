package com.pa.xpath.data;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author panderson
 */
@Embeddable
public class XPathItemPK implements Serializable {
    @Basic(optional = false)
    @Column(name = "LIBRARY_SECTION", nullable = false, length = 20)
    private String librarySection;
    @Basic(optional = false)
    @Column(name = "ITEM_NAME", nullable = false, length = 40)
    private String itemName;

    public XPathItemPK() {
    }

    public XPathItemPK(String librarySection, String itemName) {
        this.librarySection = librarySection;
        this.itemName = itemName;
    }

    public String getLibrarySection() {
        return librarySection;
    }

    public void setLibrarySection(String librarySection) {
        this.librarySection = librarySection;
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
        hash += (librarySection != null ? librarySection.hashCode() : 0);
        hash += (itemName != null ? itemName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof XPathItemPK)) {
            return false;
        }
        XPathItemPK other = (XPathItemPK) object;
        if ((this.librarySection == null && other.librarySection != null) || (this.librarySection != null && !this.librarySection.equals(other.librarySection))) {
            return false;
        }
        if ((this.itemName == null && other.itemName != null) || (this.itemName != null && !this.itemName.equals(other.itemName))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("XpathLibraryPK[ librarySection=%s, itemName=%s]",librarySection,itemName);
    }
    
}
