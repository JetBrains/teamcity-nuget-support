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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed.functions;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.MetadataConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.*;
import org.odata4j.producer.QueryInfo;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class FindPackagesByIdFunction implements NuGetFeedFunction {

  private final Logger LOG = Logger.getInstance(getClass().getName());

  @NotNull private final PackagesIndex myIndex;
  @NotNull private final EdmEntitySet.Builder myPackagesEntitySetBuilder = new EdmEntitySet.Builder().setName(MetadataConstants.ENTITY_SET_NAME);

  public FindPackagesByIdFunction(@NotNull final PackagesIndex index) {
    myIndex = index;
  }

  @NotNull
  public String getName() {
    return MetadataConstants.FIND_PACKAGES_BY_ID_FUNCTION_NAME;
  }

  @NotNull
  public EdmFunctionImport.Builder generateImport(@NotNull EdmType returnType) {
    return new EdmFunctionImport.Builder()
            .setName(MetadataConstants.FIND_PACKAGES_BY_ID_FUNCTION_NAME)
            .setEntitySet(myPackagesEntitySetBuilder)
            .setHttpMethod(MetadataConstants.HTTP_METHOD_GET)
            .setReturnType(returnType)
            .addParameters(new EdmFunctionParameter.Builder().setName(MetadataConstants.ID).setType(EdmSimpleType.STRING));
  }

  @Nullable
  public org.odata4j.producer.BaseResponse call(@NotNull EdmType returnType, @NotNull Map<String, OFunctionParameter> params, @Nullable QueryInfo queryInfo) {
    final OFunctionParameter idParam = params.get(MetadataConstants.ID);
    if(idParam == null){
      LOG.debug(String.format("Bad %s function call. ID parameter is not specified.", getName()));
      return null;
    }
    final OObject id = idParam.getValue();
    if(!(id instanceof OSimpleObject))
    {
      LOG.debug(String.format("Bad %s function call. ID parameter type is invalid.", getName()));
      return null;
    }
    final OSimpleObject idObjectCasted = (OSimpleObject) id;
    final String packageId = idObjectCasted.getValue().toString();
    final Iterator<NuGetIndexEntry> indexEntries = myIndex.getNuGetEntries(packageId);
    if(!indexEntries.hasNext()){
      LOG.debug("No packages found for id " + packageId);
      return null;
    }
    return new NuGetPackagesCollectionResponse(indexEntries, returnType);
  }
}
