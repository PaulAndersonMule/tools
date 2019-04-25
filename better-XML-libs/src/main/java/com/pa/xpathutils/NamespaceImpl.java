package com.pa.xpathutils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;

/**
 *
 * @author panderson
 */
public class NamespaceImpl implements NamespaceContext{

  @Override
  public String getNamespaceURI(String prefix) {
    return "http://nowhere.nothing.com";
  }

  @Override
  public String getPrefix(String namespaceURI) {
    return "n";
  }

  @Override
  public Iterator getPrefixes(String namespaceURI) {
    List<String> lst = new ArrayList<>();
    lst.add(getPrefix(namespaceURI));
    return lst.iterator();
  }

}
