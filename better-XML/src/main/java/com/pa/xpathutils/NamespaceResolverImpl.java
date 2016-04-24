package com.pa.xpathutils;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import net.sf.saxon.om.NamespaceResolver;

/**
 *
 * @author panderson
 */
public class NamespaceResolverImpl implements NamespaceResolver {

  private final Map<String, String> namespacePairs;
  private final Iterator<String> iterator;
  
  public NamespaceResolverImpl(String xml)
    throws XPathExpressionException{
    this(NamespaceDiscovery.discoverNamespaces(xml));
  }

  public Map<String, String> getNamespacePairs() {
    return Collections.unmodifiableMap(namespacePairs);
  }
  
  
  
  private NamespaceResolverImpl(final Map<String, String> namespacePairs){
    this.namespacePairs = Collections.unmodifiableMap(namespacePairs);
    iterator = this.namespacePairs.keySet().iterator();
  }
  
  @Override
  public String getURIForPrefix(String prefix, boolean isDefault) {
    if (isDefault){
      return namespacePairs.get("");
    }
    return namespacePairs.get(prefix);
  }

  @Override
  public Iterator<String> iteratePrefixes() {
    return iterator;
  }
}
