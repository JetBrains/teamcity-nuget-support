package org.odata4j.edm;

public class EdmAssociationSet {

  public final String name;
  public final EdmAssociation association;
  public final EdmAssociationSetEnd end1;
  public final EdmAssociationSetEnd end2;

  public EdmAssociationSet(String name, EdmAssociation association, EdmAssociationSetEnd end1, EdmAssociationSetEnd end2) {
    this.name = name;
    this.association = association;
    this.end1 = end1;
    this.end2 = end2;
  }

}
