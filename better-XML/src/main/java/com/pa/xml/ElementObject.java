package com.pa.xml;

import java.util.Objects;

/**
 *
 * @author panderson
 */
public class ElementObject extends TreeNodeObject{

  protected final String namespace;
  protected final String name;
  
  protected ElementObject(E_NODE_KIND kind, String namespace, String name){
    super(E_NODE_KIND.DOCUMENT);
    this.namespace = namespace;
    this.name = name;
  }

  protected ElementObject(String namespace, String name){
    super(E_NODE_KIND.ELEMENT);
    this.namespace = namespace;
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  protected String asXPathEntry(int counter) {
    return String.format("%s[%d]", name, counter);
//    return counter == 1? name : String.format("%s[%d]", name, counter);
  }

  
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + Objects.hashCode(this.namespace);
    hash = 89 * hash + Objects.hashCode(this.name);
    hash = 89 * hash + Objects.hashCode(this.kind);
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
    final ElementObject other = (ElementObject) obj;
    if (!Objects.equals(this.namespace, other.namespace)) {
      return false;
    }
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (this.kind != other.kind) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    if (namespace == null){
      return name;
    }
    return String.format("%s:%s", namespace, name);
  }

  
  
  
}
