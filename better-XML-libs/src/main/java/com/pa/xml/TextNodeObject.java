package com.pa.xml;

import java.util.Objects;

/**
 *
 * @author panderson
 */
public class TextNodeObject extends TreeNodeObject{
  String text;
  
  
  public TextNodeObject(String text){
    super(E_NODE_KIND.TEXT);
    this.text = text;
  }
  
  @Override
  public String getName() {
    return "text()";
  }

  @Override
  protected String asXPathEntry(int counter) {
    return "text()";
  }

  @Override
  public String getValue() {
    return text;
  }

  
  @Override
  public String toString() {
    return text;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.text);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TextNodeObject other = (TextNodeObject) obj;
    if (!Objects.equals(this.text, other.text)) {
      return false;
    }
    return true;
  }
  
  
}
