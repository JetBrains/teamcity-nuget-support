package org.odata4j.edm;

public class EdmFunctionParameter {

  public final String name;
  public final EdmType type;
  public final String mode;

  public EdmFunctionParameter(String name, EdmType type, String mode) {
    this.name = name;
    this.type = type;
    this.mode = mode;
  }
}
