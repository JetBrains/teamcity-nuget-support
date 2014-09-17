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

import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmSchema;
import org.odata4j.producer.inmemory.InMemoryComplexTypeInfo;
import org.odata4j.producer.inmemory.InMemoryEdmGenerator;
import org.odata4j.producer.inmemory.InMemoryEntityInfo;
import org.odata4j.producer.inmemory.InMemoryTypeMapping;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedInMemoryEdmGenerator extends InMemoryEdmGenerator {

  private static final String PACKAGES_ENTITY_SET_NAME = "Packages";
  private static final String HTTP_METHOD_GET = "GET";
  private static final String SEARCH_FUNCTION_NAME = "Search";
  private static final String FIND_PACKAGES_BY_ID_FUNCTION_NAME = "FindPackagesById";
  private static final String GET_UPDATES_FUNCTION_NAME = "GetUpdates";

  private EdmFunctionImport.Builder mySearchFunc;
  private EdmFunctionImport.Builder myFindPackagesByIdFunc;
  private EdmFunctionImport.Builder myGetUpdatesFunc;

  public NuGetFeedInMemoryEdmGenerator(String namespace, String containerName, InMemoryTypeMapping typeMapping,
                              String idPropertyName, Map<String, InMemoryEntityInfo<?>> eis,
                              Map<String, InMemoryComplexTypeInfo<?>> complexTypes, boolean flatten) {
    super(namespace, containerName, typeMapping, idPropertyName, eis, complexTypes, flatten);

    final EdmEntitySet.Builder packagesEntitySet = new EdmEntitySet.Builder().setName(PACKAGES_ENTITY_SET_NAME);

    mySearchFunc = new EdmFunctionImport.Builder()
            .setName(SEARCH_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(HTTP_METHOD_GET);

    myFindPackagesByIdFunc = new EdmFunctionImport.Builder()
            .setName(FIND_PACKAGES_BY_ID_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(HTTP_METHOD_GET);

    myGetUpdatesFunc = new EdmFunctionImport.Builder()
            .setName(GET_UPDATES_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(HTTP_METHOD_GET);
  }

  @Override
  protected void addFunctions(EdmSchema.Builder schema, EdmEntityContainer.Builder container) {
    super.addFunctions(schema, container);
    container.addFunctionImports(mySearchFunc, myFindPackagesByIdFunc, myGetUpdatesFunc);
  }
}
