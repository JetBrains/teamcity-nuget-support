package org.odata4j.edm;

public class EdmNavigationProperty extends EdmPropertyBase {

  public final EdmAssociation relationship;
  public final EdmAssociationEnd fromRole;
  public final EdmAssociationEnd toRole;

  public EdmNavigationProperty(
      String name,
      EdmAssociation relationship,
      EdmAssociationEnd fromRole,
      EdmAssociationEnd toRole) {
    super(name);
    this.relationship = relationship;
    this.fromRole = fromRole;
    this.toRole = toRole;
  }

  @Override
  public String toString() {
    return String.format("EdmNavigationProperty[%s,rel=%s,from=%s,to=%s]", name, relationship, fromRole, toRole);
  }
}
