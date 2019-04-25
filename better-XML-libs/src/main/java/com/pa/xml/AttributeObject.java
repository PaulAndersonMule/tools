package com.pa.xml;

import java.util.Objects;
/**
 *
 * @author panderson
 */
public class AttributeObject extends TreeNodeObject{

  private final String namespace;
  private final String name;
  private final String value;
  
  protected AttributeObject(String namespace, String name, String value){
    super(E_NODE_KIND.ATTRIBUTE);
    this.namespace = namespace;
    this.name = name;
    this.value = value;
  }

  @Override
  protected String asXPathEntry(int counter) {
    return String.format("@%s", name);
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
  public String getName() {
    return name;
  }
  
  @Override
  public String toString() {
    if (namespace == null){
      return String.format("%s: %s", name, value);
    }
      return String.format("%s:%s=\"%s\"", namespace, name, value);
  }

  @Override
  public String getValue() {
    return value; 
  }

  
  
  
}
