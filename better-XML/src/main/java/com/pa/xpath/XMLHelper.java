package com.pa.xpath;

import com.pa.xpathutils.NamespaceResolverImpl;
import com.pa.xpathutils.XPathUtils;
import java.util.List;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;

/**
 *
 * @author panderson
 */
public abstract class XMLHelper {

  protected static String getXPathResults(NamespaceResolverImpl nsrImpl, String xpath, String fileContents) {
    List<XdmItem> xdmItems;
    try {
      xdmItems = XPathUtils.evaluate(xpath, fileContents, nsrImpl);
    } catch (SaxonApiException ex) {
      return ex.getMessage();
    }
    StringBuilder sb = new StringBuilder();
		xdmItems.stream().forEach((item) -> {
			sb.append(item.toString()).append('\n');
		});
    
    return sb.toString();
  }
}
