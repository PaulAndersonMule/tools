package com.pa.xpathutils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.apache.commons.io.FileUtils;
import org.xml.sax.InputSource;

/**
 *
 * @author panderson
 */
public class NamespaceDiscovery {

  public static Map<String, String> discoverNamespaces(String xml) 
  throws XPathExpressionException{
    XPathFactoryImpl xx = new XPathFactoryImpl();
    
    XPath xp = xx.newXPath();
    
    String xpath = "for $i in distinct-values(\n" +
"             for $ns in //namespace::*\n" +
"               return\n" +
"                  index-of(\n" +
"                           (for $x in //namespace::*\n" +
"                             return\n" +
"                                concat(name($x), ' ', string($x))\n" +
"                            ),\n" +
"                            concat(name($ns), ' ', string($ns))\n" +
"                          )\n" +
"                          [1]\n" +
"                                                  )\n" +
"  return\n" +
"    for $x in (//namespace::*)[$i]\n" +
"     return\n" +
"        concat(name($x), ',', string($x))";
    InputSource is = new InputSource(new ByteArrayInputStream(xml.getBytes()));

    List<String> namespacesData = (List<String>)xp.evaluate(xpath, is, XPathConstants.NODESET);
    Map<String, String> results = new HashMap<>();
    
    for (String item : namespacesData){
      String[] items = item.split(",");
      String ns = items[0].trim();
      if (ns.equals("")){
        ns = "n";
      }
      results.put(ns, items[1].trim());
    }
    return results;
  }
  
  public static void main(String[] args)
    throws Exception{
    String xml = FileUtils.readFileToString(new File("C:\\projects\\utils\\utils\\src\\main\\java\\xml\\sampleFlow.xml"));
    discoverNamespaces(xml);
  }
}
