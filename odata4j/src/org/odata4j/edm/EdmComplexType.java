package org.odata4j.edm;

import java.util.List;

public class EdmComplexType extends EdmStructuralType {

  public EdmComplexType(String namespace, String name, List<EdmProperty> properties) {
    super(null, namespace, name, properties);
  }
  
}
