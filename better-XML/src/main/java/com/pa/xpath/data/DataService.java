package com.pa.xpath.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 *
 * @author panderson
 */
public class DataService {

  private static final EntityManager em = Persistence.createEntityManagerFactory("com.pa.xpath_xpath20Tool_PU").createEntityManager();
  
  public static List<XPathLibrarySection> getLibrarySections(){
    List<XPathLibrarySection> res = em.createNamedQuery("XPathLibrarySection.findAll").getResultList();
    Collections.sort(res);
    return res;
  }
  
  public static XPathLibrarySection getLibrarySection(String name){
    List<XPathLibrarySection> res = em.createNamedQuery("XPathLibrarySection.findBySectionName").setParameter("name", name).getResultList();
    if (res.isEmpty()){
      return null;
    }
    return res.get(0);
  }
  
  public static List<XPathItem> getXpathLibraryItems(String librarySection){
    List<XPathItem> res = em.createNamedQuery("XPathItem.findByLibrarySection").setParameter("librarySection", librarySection).getResultList();
    Collections.sort(res);
    return res;
  }
  
  public static XPathLibrarySection createLibrarySection(String name){
    XPathLibrarySection section = new XPathLibrarySection(name);
    em.getTransaction().begin();
    em.persist(section);
    em.getTransaction().commit();
    em.detach(section);
    return section;
  }
  
  public static XPathItem createXPathEntry(String sectionName, String itemName, String xpathEntry){
    em.getTransaction().begin();
    XPathItem entry = new XPathItem(sectionName, itemName);
    entry.setXpath(xpathEntry);
    em.persist(entry);
    em.getTransaction().commit();
    em.detach(entry);
    return entry;
  }
  
  public static XPathItem createXPathEntryLazy(String sectionName, String itemName, String xpathEntry){
    em.getTransaction().begin();
    XPathLibrarySection section = new XPathLibrarySection(sectionName);
    XPathItem entry = new XPathItem(sectionName, itemName);
    entry.setLibrarySection1(section);
    entry.setXpathLibraryPK(new XPathItemPK(sectionName, itemName));
    section.setXpathItemList(new ArrayList<>());
    section.getXpathLibraryList().add(entry);
    entry.setXpath(xpathEntry);
    em.persist(entry);
    em.getTransaction().commit();
    return entry;
  }
  
  public static void deleteXPathItem(String sectionName, String xpathItemName){
    XPathItemPK key = new XPathItemPK(sectionName, xpathItemName);
    XPathItem toRemove = em.find(XPathItem.class, key);
    em.getTransaction().begin();
    em.remove(toRemove);
    em.getTransaction().commit();
  }
  
  public static void updateXPathItem(String sectionName, String xpathItemName, String xpath){
    em.getTransaction().begin();
    XPathLibrarySection section = em.find(XPathLibrarySection.class, sectionName);//, LockModeType.PESSIMISTIC_WRITE);
    XPathItemPK key = new XPathItemPK(sectionName, xpathItemName);
    XPathItem xpathLibItem = em.find(XPathItem.class, key);
    xpathLibItem.setXpath(xpath);
    em.merge(xpathLibItem);
    em.getTransaction().commit();
  }
  
  public static void deleteXPathLibrarySection(String name){
    em.getTransaction().begin();
    XPathLibrarySection section = em.find(XPathLibrarySection.class, name);
    em.remove(section);
    em.getTransaction().commit();
  }

	
	
	
	
	
	  public static List<XSLTLibrarySection> getXSLTLibrarySections(){
    List<XSLTLibrarySection> res = em.createNamedQuery("XSLTLibrarySection.findAll").getResultList();
    Collections.sort(res);
    return res;
  }
  
  public static XPathLibrarySection getXSLTLibrarySection(String name){
    List<XPathLibrarySection> res = em.createNamedQuery("XSLTLibrarySection.findBySectionName").setParameter("name", name).getResultList();
    if (res.isEmpty()){
      return null;
    }
    return res.get(0);
  }
  
  public static List<XSLTItem> getXSLTLibraryItems(String librarySection){
    List<XSLTItem> res = em.createNamedQuery("XSLTItem.findByXSLTLibrarySection").setParameter("xsltLibrarySection", librarySection).getResultList();
    Collections.sort(res);
    return res;
  }
  
  public static XSLTLibrarySection createXSLTLibrarySection(String name){
    XSLTLibrarySection section = new XSLTLibrarySection(name);
    em.getTransaction().begin();
    em.persist(section);
    em.getTransaction().commit();
    em.detach(section);
    return section;
  }
  
  public static XSLTItem createXSLTEntry(String sectionName, String itemName, String xslt){
    em.getTransaction().begin();
    XSLTItem entry = new XSLTItem(sectionName, itemName);
    entry.setXSLT(xslt);
    em.persist(entry);
    em.getTransaction().commit();
    em.detach(entry);
    return entry;
  }
  
  public static XSLTItem createXSLTEntryLazy(String sectionName, String itemName, String xslt){
    em.getTransaction().begin();
    XSLTLibrarySection section = new XSLTLibrarySection(sectionName);
    XSLTItem entry = new XSLTItem(sectionName, itemName);
		entry.setXSLTLibrarySection(section);
		entry.setXSLTLibraryPK(new XSLTItemPK(sectionName, itemName));
		section.setXSLTLibraryList(new ArrayList<>());
		section.getXSLTLibraryList().add(entry);
    entry.setXSLT(xslt);
    em.persist(section);
    em.getTransaction().commit();
    return entry;
  }
  
  public static void deleteXSLTItem(String sectionName, String xsltItemName){
    XSLTItemPK key = new XSLTItemPK(sectionName, xsltItemName);
    XSLTItem toRemove = em.find(XSLTItem.class, key);
    em.getTransaction().begin();
    em.remove(toRemove);
    em.getTransaction().commit();
  }
  
  public static void updateXSLTItem(String sectionName, String xsltItemName, String xslt){
    em.getTransaction().begin();
    XSLTLibrarySection section = em.find(XSLTLibrarySection.class, sectionName);//, LockModeType.PESSIMISTIC_WRITE);
    XSLTItemPK key = new XSLTItemPK(sectionName, xsltItemName);
    XSLTItem xsltLibItem = em.find(XSLTItem.class, key);
    xsltLibItem.setXSLT(xslt);
    em.merge(xsltLibItem);
    em.getTransaction().commit();
  }
  
  public static void deleteXSLTLibrarySection(String name){
    em.getTransaction().begin();
    XSLTLibrarySection section = em.find(XSLTLibrarySection.class, name);
    em.remove(section);
    em.getTransaction().commit();
  }

	
	
	
	
	
}
