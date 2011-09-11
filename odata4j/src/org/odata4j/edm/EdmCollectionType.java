package org.odata4j.edm;

/**
 * Describes a homogeneous collection of instances of a specific type.
 */
public class EdmCollectionType extends EdmNonSimpleType {

  private final EdmType collectionType;
  
  public EdmCollectionType(String fullyQualifiedTypeName, EdmType collectionType) {
    super(fullyQualifiedTypeName);
    if (collectionType == null) throw new IllegalArgumentException("collectionType cannot be null");
    this.collectionType = collectionType;
  }
  
  public EdmType getCollectionType() {
    return collectionType;
  }
}
