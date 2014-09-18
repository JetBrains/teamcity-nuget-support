/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.server.feed.server.javaFeed;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;
import org.odata4j.edm.*;
import org.odata4j.producer.inmemory.InMemoryEdmGenerator;
import org.odata4j.producer.inmemory.InMemoryEntityInfo;
import org.odata4j.producer.inmemory.InMemoryTypeMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedInMemoryEdmGenerator extends InMemoryEdmGenerator {

  private static final String SEARCH_TERM = "searchTerm";
  private static final String TARGET_FRAMEWORK = "targetFramework";
  private static final String INCLUDE_PRERELEASE = "includePrerelease";
  private static final String PACKAGE_IDS = "packageIds";
  private static final String VERSIONS = "versions";
  private static final String INCLUDE_ALL_VERSIONS = "includeAllVersions";
  private static final String TARGET_FRAMEWORKS = "targetFrameworks";
  private static final String VERSION_CONSTRAINTS = "versionConstraints";
  private static final String ID = "id";

  private final Logger LOG = Logger.getInstance(getClass().getName());

  public NuGetFeedInMemoryEdmGenerator(String namespace, String containerName, InMemoryTypeMapping typeMapping,
                              String idPropertyName, Map<String, InMemoryEntityInfo<?>> eis) {
    super(namespace, containerName, typeMapping, idPropertyName, eis);
  }

  @Override
  public EdmDataServices.Builder generateEdm(EdmDecorator decorator) {
    final EdmDataServices.Builder edmBuilder = super.generateEdm(decorator);
    boolean setFuncImports = false;
    for(EdmSchema.Builder schemaBuilder : edmBuilder.getSchemas()){
      if(schemaBuilder.getNamespace().equalsIgnoreCase(MetadataConstants.NUGET_GALLERY_NAMESPACE)){
        final EdmEntityType.Builder entityTypeBuilder = CollectionsUtil.findFirst(schemaBuilder.getEntityTypes(), new Filter<EdmEntityType.Builder>() {
          public boolean accept(@NotNull EdmEntityType.Builder data) {
            return data.getName().equalsIgnoreCase(MetadataConstants.ENTITY_TYPE_NAME);
          }
        });
        if(entityTypeBuilder != null) {
          for(EdmEntityContainer.Builder entityContainerBuilder : schemaBuilder.getEntityContainers()){
            if(entityContainerBuilder.getName().equalsIgnoreCase(MetadataConstants.CONTAINER_NAME)){
              entityContainerBuilder.addFunctionImports(generateNugetAPIv2FunctionImports(entityTypeBuilder.build()));
              setFuncImports = true;
            }
          }
        }
      }
    }

    if(setFuncImports)
      LOG.debug("NuGet API v2 function imports were setted up succesfully.");
    else
      LOG.warn("NuGet API v2 function imports were NOT setted up.");

    return edmBuilder;
  }

  private List<EdmFunctionImport.Builder> generateNugetAPIv2FunctionImports(EdmEntityType entityType) {
    final EdmEntitySet.Builder packagesEntitySet = new EdmEntitySet.Builder().setName(MetadataConstants.ENTITY_SET_NAME);
    final EdmType packagesCollectionType = new EdmCollectionType(EdmProperty.CollectionKind.Collection, entityType);
    final List<EdmFunctionImport.Builder> result = new ArrayList<EdmFunctionImport.Builder>();

    result.add(new EdmFunctionImport.Builder()
            .setName(MetadataConstants.SEARCH_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(MetadataConstants.HTTP_METHOD_GET)
            .setReturnType(packagesCollectionType)
            .addParameters(new EdmFunctionParameter.Builder().setName(SEARCH_TERM).setType(EdmSimpleType.STRING),
                    new EdmFunctionParameter.Builder().setName(TARGET_FRAMEWORK).setType(EdmSimpleType.STRING),
                    new EdmFunctionParameter.Builder().setName(INCLUDE_PRERELEASE).setType(EdmSimpleType.BOOLEAN)));

    result.add(new EdmFunctionImport.Builder()
            .setName(MetadataConstants.FIND_PACKAGES_BY_ID_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(MetadataConstants.HTTP_METHOD_GET)
            .setReturnType(packagesCollectionType)
            .addParameters(new EdmFunctionParameter.Builder().setName(ID).setType(EdmSimpleType.STRING)));

    result.add(new EdmFunctionImport.Builder()
            .setName(MetadataConstants.GET_UPDATES_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(MetadataConstants.HTTP_METHOD_GET)
            .setReturnType(packagesCollectionType)
            .addParameters(new EdmFunctionParameter.Builder().setName(PACKAGE_IDS).setType(EdmSimpleType.STRING),
                    new EdmFunctionParameter.Builder().setName(VERSIONS).setType(EdmSimpleType.STRING),
                    new EdmFunctionParameter.Builder().setName(INCLUDE_PRERELEASE).setType(EdmSimpleType.BOOLEAN),
                    new EdmFunctionParameter.Builder().setName(INCLUDE_ALL_VERSIONS).setType(EdmSimpleType.BOOLEAN),
                    new EdmFunctionParameter.Builder().setName(TARGET_FRAMEWORKS).setType(EdmSimpleType.STRING),
                    new EdmFunctionParameter.Builder().setName(VERSION_CONSTRAINTS).setType(EdmSimpleType.STRING)));

    return result;
  }
}
