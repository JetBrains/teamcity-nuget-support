package org.odata4j.edm;

public class EdmAssociationSetEnd {

  public final EdmAssociationEnd role;
  public final EdmEntitySet entitySet;

  public EdmAssociationSetEnd(EdmAssociationEnd role, EdmEntitySet entitySet) {
    this.role = role;
    this.entitySet = entitySet;
  }
}
