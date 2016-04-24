package com.pa.xpath;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

/**
 *
 * @author panderson
 */
public class XSLTHelper {

  public static String transformXML(String xml, String xslt) throws Exception{

    XdmNode source;
    Processor proc2 = new Processor(false);
    XsltCompiler comp = proc2.newXsltCompiler();
    StringReader sr = new StringReader(xslt);
    XsltExecutable exec = comp.compile(new StreamSource(sr));
    source = proc2.newDocumentBuilder().build(new StreamSource(new StringReader(xml)));

    XsltTransformer tran = exec.load();
		Processor processor = new Processor(false);
    Serializer out = processor.newSerializer();
//    out.setOutputProperty(Serializer.Property.METHOD, "html");
    out.setOutputProperty(Serializer.Property.INDENT, "yes");
    OutputStream os = new ByteArrayOutputStream();
    out.setOutputStream(os);
    tran.setInitialContextNode(source);
    tran.setDestination(out);
    try {
      tran.transform();
    } catch (SaxonApiException ex) {
      return ex.getMessage();
    }
    return os.toString();
  }
}
