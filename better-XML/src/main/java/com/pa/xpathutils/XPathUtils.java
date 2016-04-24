package com.pa.xpathutils;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.tree.tiny.TinyTextImpl;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.xml.sax.InputSource;

/**
 *
 * @author panderson
 */
public abstract class XPathUtils {

  private static final Processor p = new Processor(false);
  private static final DocumentBuilder docBuilder = p.newDocumentBuilder();
  private static final XPathCompiler xpathCompiler = p.newXPathCompiler();

  public static List<XdmItem> evaluate(String xpath, String xml, NamespaceResolverImpl nsrImpl) 
    throws SaxonApiException{
    Source s = new StreamSource(new StringReader(xml));

		nsrImpl.getNamespacePairs().entrySet().stream().forEach((entry) -> {
			xpathCompiler.declareNamespace(entry.getKey(), entry.getValue());
		});
    
    XdmNode start = docBuilder.build(s);
    XPathSelector xpSel = xpathCompiler.compile(xpath).load();
    xpSel.setContextItem(start);
    Iterator<XdmItem> it = xpSel.iterator();
    List<XdmItem> items = new ArrayList<>();

    while (it.hasNext()) {
      items.add(it.next());
    }
    return items;
  }

  private static List evalXPath(NamespaceContext nsctx, Object xml, String xpath)
          throws XPathExpressionException {
    XPathFactoryImpl xx = new XPathFactoryImpl();

    XPath xp = xx.newXPath();

    xp.setNamespaceContext(nsctx);
    XPathExpression xpe = xp.compile(xpath);
    InputSource is;

    if (xml instanceof String) {
      is = new InputSource(new ByteArrayInputStream(xml.toString().getBytes()));
      return (List) xpe.evaluate(is, XPathConstants.NODESET);
    } else {
      if (1 == 1) {
        throw new RuntimeException("");
      }
      return (List) xpe.evaluate(xml, XPathConstants.NODESET);
    }

  }

  public static List<String> evalXPathAsStrings(NamespaceContext nsctx, String xml, String xpath)
          throws XPathExpressionException {
    List resultsT = evalXPath(nsctx, xml, xpath);

    List<String> results = new ArrayList<>();
		resultsT.stream().forEach((o) -> {
			results.add(((TinyTextImpl) o).getStringValue());
		});
    return results;
  }

  public static List<TinyElementImpl> evalXPathAsTinyElements(NamespaceContext nsctx, String xml, String xpath)
          throws XPathExpressionException {
    return (List<TinyElementImpl>) evalXPath(nsctx, xml, xpath);
  }

}