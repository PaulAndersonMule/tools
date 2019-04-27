package com.pa.xpath;

import com.pa.xpathutils.NamespaceResolverImpl;
import com.pa.xpathutils.XPathUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;

/**
 *
 * @author panderson
 */
public abstract class XMLHelper {

  private static List<XdmItem> internalGetXPathResult(NamespaceResolverImpl nsrImpl, String xpath, String fileContents) throws SaxonApiException {
      return XPathUtils.evaluate(xpath, fileContents, nsrImpl);
  }

  protected static String getXPathResults(NamespaceResolverImpl nsrImpl, String xpath, String fileContents) {
    List<XdmItem> xdmItems = null;
    try {
        xdmItems = internalGetXPathResult(nsrImpl, xpath, fileContents);
    } catch (SaxonApiException e) {
        return e.getMessage();
    }

    StringBuilder sb = new StringBuilder();
		xdmItems.stream().forEach((item) -> {
			sb.append(item.toString()).append('\n');
		});
    
    return sb.toString();
  }

  public static List<String> getXPathResultList(NamespaceResolverImpl nsrImpl, String xpath, String fileContents) {
        List<XdmItem> xdmItems = null;
        List<String> result = new ArrayList<>();
        try {
            xdmItems = internalGetXPathResult(nsrImpl, xpath, fileContents);
        } catch (SaxonApiException e) {
            result.add(e.getMessage());
            return result;
        }
        for (XdmItem item : xdmItems) {
            result.add(item.toString());
        }
        return result;
    }

}
