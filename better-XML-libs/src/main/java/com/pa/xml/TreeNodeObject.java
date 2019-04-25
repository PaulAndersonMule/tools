package com.pa.xml;

/**
 *
 * @author panderson
 */
public abstract class TreeNodeObject {

  public enum E_NODE_KIND {

    DOCUMENT,
    ELEMENT,
    ATTRIBUTE,
    TEXT;
  }

  protected E_NODE_KIND kind;

  private static final String COLON = ":";

  protected TreeNodeObject() {
  }

  public final E_NODE_KIND getNodeKind() {
    return kind;
  }

  protected TreeNodeObject(E_NODE_KIND kind) {
    this.kind = kind;
  }

  public final String buildPath(boolean useExplicitDefaultNamespace, int counter) {
    String pattern = useExplicitDefaultNamespace ? "n:%s" : "%s";
    if (!getName().contains(COLON) && kind.equals(E_NODE_KIND.ELEMENT)) {
      String x = String.format(pattern, asXPathEntry(counter));
      return x;
    } else if (kind.equals(E_NODE_KIND.DOCUMENT)) {
      if (getName().contains(COLON)) {
        return getName();
      }
      return String.format(pattern, getName());
    }
    return asXPathEntry(counter);
  }

  @Override
  public abstract String toString();

  protected abstract String asXPathEntry(int counter);

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(Object obj);

  public abstract String getName();

  public String getValue() {
    return getName();
  }
}
