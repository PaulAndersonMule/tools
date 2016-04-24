package com.pa.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author panderson
 */
public class SAXHandler extends DefaultHandler {

  private DefaultMutableTreeNode currentNode;
  private final DefaultTreeModel treeModel = new DefaultTreeModel(null);
  private boolean haveRootNode = false;
  private final List<DefaultMutableTreeNode> stack = new ArrayList<>();

  public DefaultTreeModel getTreeModel() {
    return treeModel;
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    super.characters(ch, start, length);
    StringBuilder textSB = new StringBuilder();
    textSB.append(ch, start, length);
    String text = textSB.toString().replaceAll("\\n", "").replace("\r", "").trim();

    if (!text.equals("")) {
      DefaultMutableTreeNode textNode = new DefaultMutableTreeNode(new TextNodeObject(text));
      treeModel.insertNodeInto(textNode, stack.get(stack.size() - 1), 0);
    }
  }

  @Override
  public void endDocument() throws SAXException {
    super.endDocument(); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    super.endElement(uri, localName, qName); //To change body of generated methods, choose Tools | Templates.
    stack.remove(stack.size() - 1);
  }

  private void buildNodeAttribs(DefaultMutableTreeNode parent, Attributes attribs) {
    int nAttribs = attribs.getLength();
    for (int i = 0; i < nAttribs; i++) {
      TreeNodeObject o = new AttributeObject(null, attribs.getQName(i), attribs.getValue(i));
      DefaultMutableTreeNode attrib = new DefaultMutableTreeNode(o);
      currentNode.add(attrib);
    }
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    super.startElement(uri, localName, qName, attributes);
    if (!haveRootNode) {
      currentNode = new DefaultMutableTreeNode(new ElementObject(TreeNodeObject.E_NODE_KIND.DOCUMENT, null, qName));
    } else {
      currentNode = new DefaultMutableTreeNode(new ElementObject(null, qName));
    }

    stack.add(currentNode);
    if (!haveRootNode) {
      treeModel.setRoot(currentNode);
      haveRootNode = true;
    } else {
      DefaultMutableTreeNode parent = stack.get(stack.size() - 2);
      int insertAt = parent.getChildCount();
      treeModel.insertNodeInto(currentNode, parent, insertAt);
    }
    buildNodeAttribs(currentNode, attributes);
  }

  @Override
  public void startDocument() throws SAXException {
    super.startDocument(); //To change body of generated methods, choose Tools | Templates.
  }

}
