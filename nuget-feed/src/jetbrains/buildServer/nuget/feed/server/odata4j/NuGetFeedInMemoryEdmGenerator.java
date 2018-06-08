/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.odata4j;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.feed.server.MetadataConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feed.server.odata4j.functions.NuGetFeedFunction;
import jetbrains.buildServer.nuget.feed.server.odata4j.functions.NuGetFeedFunctions;
import jetbrains.buildServer.util.CollectionsUtil;
import org.odata4j.edm.*;
import org.odata4j.producer.inmemory.InMemoryComplexTypeInfo;
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

  private final Logger LOG = Logger.getInstance(getClass().getName());
  private final NuGetFeedFunctions myFunctions;
  private final NuGetAPIVersion myApiVersion;

  public NuGetFeedInMemoryEdmGenerator(String namespace, String containerName, InMemoryTypeMapping typeMapping,
                                       String idPropertyName, Map<String, InMemoryEntityInfo<?>> eis, Map<String,
    InMemoryComplexTypeInfo<?>> complexTypes, NuGetFeedFunctions functions, NuGetAPIVersion apiVersion) {
    super(namespace, containerName, typeMapping, idPropertyName, eis, complexTypes);
    myFunctions = functions;
    myApiVersion = apiVersion;
  }

  @Override
  public EdmDataServices.Builder generateEdm(EdmDecorator decorator) {
    final EdmDataServices.Builder edmBuilder = super.generateEdm(decorator);
    if(myApiVersion == NuGetAPIVersion.V2) {
      LOG.debug("Generating NuGet API v2 function imports.");
      boolean setFuncImports = false;
      for(EdmSchema.Builder schemaBuilder : edmBuilder.getSchemas()){
        if(schemaBuilder.getNamespace().equalsIgnoreCase(MetadataConstants.NUGET_GALLERY_NAMESPACE)){
          final EdmEntityType.Builder entityTypeBuilder = CollectionsUtil.findFirst(schemaBuilder.getEntityTypes(), data -> data.getName().equalsIgnoreCase(myApiVersion.name() + MetadataConstants.ENTITY_TYPE_NAME));
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
        LOG.debug("NuGet API v2 function imports were setted up successfully.");
      else
        LOG.warn("NuGet API v2 function imports were NOT setted up.");
    }
    return edmBuilder;
  }

  private List<EdmFunctionImport.Builder> generateNugetAPIv2FunctionImports(EdmEntityType entityType) {
    final List<EdmFunctionImport.Builder> result = new ArrayList<>();
    for(NuGetFeedFunction function : myFunctions.getAll()){
      result.add(function.generateImport(entityType));
    }
    return result;
  }
}
