package org.odata4j.edm;

/**
 * Non-primitive type in the EDM type system.
 */
public class EdmNonSimpleType extends EdmType {

  public EdmNonSimpleType(String fullyQualifiedTypeName) {
    super(fullyQualifiedTypeName);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

}
