package org.odata4j.edm;

import java.util.List;

import org.core4j.Enumerable;

public class EdmEntityContainer {

  public final String name;
  public final boolean isDefault;
  public final Boolean lazyLoadingEnabled;
  public final List<EdmEntitySet> entitySets;
  public final List<EdmAssociationSet> associationSets;
  public final List<EdmFunctionImport> functionImports;

  public EdmEntityContainer(String name, boolean isDefault, Boolean lazyLoadingEnabled, List<EdmEntitySet> entitySets, List<EdmAssociationSet> associationSets, List<EdmFunctionImport> functionImports) {
    this.name = name;
    this.isDefault = isDefault;
    this.lazyLoadingEnabled = lazyLoadingEnabled;
    this.entitySets = entitySets == null ? Enumerable.empty(EdmEntitySet.class).toList() : entitySets;
    this.associationSets = associationSets == null ? Enumerable.empty(EdmAssociationSet.class).toList() : associationSets;
    this.functionImports = functionImports == null ? Enumerable.empty(EdmFunctionImport.class).toList() : functionImports;
  }
}
