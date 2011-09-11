package org.odata4j.edm;

public class EdmAssociation {

  public final String namespace;
  public final String alias;
  public final String name;
  public final EdmAssociationEnd end1;
  public final EdmAssociationEnd end2;

  public EdmAssociation(String namespace, String alias, String name, EdmAssociationEnd end1, EdmAssociationEnd end2) {
    this.namespace = namespace;
    this.alias = alias;
    this.name = name;
    this.end1 = end1;
    this.end2 = end2;
  }

  public String getFQNamespaceName() {
    return namespace + "." + name;
  }

  public String getFQAliasName() {
    return alias == null ? null : (alias + "." + name);
  }

  @Override
  public String toString() {
    StringBuilder rt = new StringBuilder();
    rt.append("EdmAssociation[");
    if (namespace != null)
      rt.append(namespace + ".");
    rt.append(name);
    if (alias != null)
      rt.append(",alias=" + alias);
    rt.append(",end1=" + end1);
    rt.append(",end2=" + end2);
    rt.append(']');
    return rt.toString();
  }
}
