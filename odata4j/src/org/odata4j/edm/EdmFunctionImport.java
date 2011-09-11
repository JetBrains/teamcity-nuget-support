package org.odata4j.edm;

import java.util.List;

public class EdmFunctionImport {

  public final String name;
  public final EdmEntitySet entitySet;
  public final EdmType returnType;
  public final String httpMethod;
  public final List<EdmFunctionParameter> parameters;

  public EdmFunctionImport(String name, EdmEntitySet entitySet, EdmType returnType,
      String httpMethod, List<EdmFunctionParameter> parameters) {
    this.name = name;
    this.entitySet = entitySet;
    this.returnType = returnType;
    this.httpMethod = httpMethod;
    this.parameters = parameters;
  }
}
