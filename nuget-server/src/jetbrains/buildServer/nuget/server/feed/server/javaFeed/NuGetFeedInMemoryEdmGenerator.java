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

import org.odata4j.edm.*;
import org.odata4j.producer.inmemory.InMemoryEdmGenerator;
import org.odata4j.producer.inmemory.InMemoryEntityInfo;
import org.odata4j.producer.inmemory.InMemoryTypeMapping;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedInMemoryEdmGenerator extends InMemoryEdmGenerator {
  private EdmFunctionImport.Builder mySearchFunc;
  private EdmFunctionImport.Builder myFindPackagesByIdFunc;
  private EdmFunctionImport.Builder myGetUpdatesFunc;

  public NuGetFeedInMemoryEdmGenerator(String namespace, String containerName, InMemoryTypeMapping typeMapping,
                              String idPropertyName, Map<String, InMemoryEntityInfo<?>> eis) {
    super(namespace, containerName, typeMapping, idPropertyName, eis);

    final EdmEntitySet.Builder packagesEntitySet = new EdmEntitySet.Builder().setName(MetadataConstants.ENTITY_SET_NAME);

    mySearchFunc = new EdmFunctionImport.Builder()
            .setName(MetadataConstants.SEARCH_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(MetadataConstants.HTTP_METHOD_GET);

    myFindPackagesByIdFunc = new EdmFunctionImport.Builder()
            .setName(MetadataConstants.FIND_PACKAGES_BY_ID_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(MetadataConstants.HTTP_METHOD_GET);

    myGetUpdatesFunc = new EdmFunctionImport.Builder()
            .setName(MetadataConstants.GET_UPDATES_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(MetadataConstants.HTTP_METHOD_GET);
  }

  @Override
  public EdmDataServices.Builder generateEdm(EdmDecorator decorator) {
    final EdmDataServices.Builder edmBuilder = super.generateEdm(decorator);
    for(EdmSchema.Builder schemaBuilder : edmBuilder.getSchemas()){
      if(schemaBuilder.getNamespace().equalsIgnoreCase(MetadataConstants.NUGET_GALLERY_NAMESPACE)){
        for(EdmEntityContainer.Builder entityContainerBuilder : schemaBuilder.getEntityContainers()){
          if(entityContainerBuilder.getName().equalsIgnoreCase(MetadataConstants.CONTAINER_NAME))
          entityContainerBuilder.addFunctionImports(mySearchFunc, myFindPackagesByIdFunc, myGetUpdatesFunc);
        }
      }
    }
    return edmBuilder;
  }
}
