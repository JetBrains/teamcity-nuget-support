package org.odata4j.edm;

import java.util.List;

import org.core4j.Enumerable;

public class EdmSchema {

  public final String namespace;
  public final String alias;
  public final List<EdmEntityType> entityTypes;
  public final List<EdmComplexType> complexTypes;
  public final List<EdmAssociation> associations;
  public final List<EdmEntityContainer> entityContainers;

  public EdmSchema(String namespace, String alias, List<EdmEntityType> entityTypes, List<EdmComplexType> complexTypes, List<EdmAssociation> associations, List<EdmEntityContainer> entityContainers) {
    this.namespace = namespace;
    this.alias = alias;
    this.entityTypes = entityTypes == null ? Enumerable.empty(EdmEntityType.class).toList() : entityTypes;
    this.complexTypes = complexTypes == null ? Enumerable.empty(EdmComplexType.class).toList() : complexTypes;
    this.associations = associations == null ? Enumerable.empty(EdmAssociation.class).toList() : associations;
    this.entityContainers = entityContainers == null ? Enumerable.empty(EdmEntityContainer.class).toList() : entityContainers;
  }

  public EdmEntityContainer findEntityContainer(String name) {
    for (EdmEntityContainer ec : this.entityContainers) {
      if (ec.name.equals(name)) {
        return ec;
      }
    }
    return null;
  }
}
