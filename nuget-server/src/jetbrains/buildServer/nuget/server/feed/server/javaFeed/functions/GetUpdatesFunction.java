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
import jetbrains.buildServer.nuget.server.feed.server.index.impl.SemanticVersionsComparer;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.MetadataConstants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.*;
import org.odata4j.producer.QueryInfo;

import java.util.*;

/**
 * @author Evgeniy.Koshkin
 */
public class GetUpdatesFunction implements NuGetFeedFunction {

  private static final String COLLECTION_VALUE_SEPARATOR = "|";
  private static final Comparator<String> SEMANTIC_VERSIONS_COMPARATOR = SemanticVersionsComparer.getSemanticVersionsComparator();

  private final Logger LOG = Logger.getInstance(getClass().getName());

  @NotNull private final PackagesIndex myIndex;

  public GetUpdatesFunction(@NotNull PackagesIndex index) {
    myIndex = index;
  }

  @NotNull
  public String getName() {
    return MetadataConstants.GET_UPDATES_FUNCTION_NAME;
  }

  @NotNull
  public EdmFunctionImport.Builder generateImport(@NotNull EdmType returnType) {
    final EdmEntitySet.Builder packagesEntitySet = new EdmEntitySet.Builder().setName(MetadataConstants.ENTITY_SET_NAME);
    return new EdmFunctionImport.Builder()
            .setName(MetadataConstants.GET_UPDATES_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(MetadataConstants.HTTP_METHOD_GET)
            .setReturnType(returnType)
            .addParameters(new EdmFunctionParameter.Builder().setName(MetadataConstants.PACKAGE_IDS).setType(EdmSimpleType.STRING),
                    new EdmFunctionParameter.Builder().setName(MetadataConstants.VERSIONS).setType(EdmSimpleType.STRING),
                    new EdmFunctionParameter.Builder().setName(MetadataConstants.INCLUDE_PRERELEASE).setType(EdmSimpleType.BOOLEAN),
                    new EdmFunctionParameter.Builder().setName(MetadataConstants.INCLUDE_ALL_VERSIONS).setType(EdmSimpleType.BOOLEAN),
                    new EdmFunctionParameter.Builder().setName(MetadataConstants.TARGET_FRAMEWORKS).setType(EdmSimpleType.STRING),
                    new EdmFunctionParameter.Builder().setName(MetadataConstants.VERSION_CONSTRAINTS).setType(EdmSimpleType.STRING));
  }

  @Nullable
  public org.odata4j.producer.BaseResponse call(@NotNull EdmType returnType, @NotNull Map<String, OFunctionParameter> params, @Nullable QueryInfo queryInfo) {
    final List<String> packageIds = extractListOfStringsFromParamValue(params, MetadataConstants.PACKAGE_IDS);
    if(packageIds.isEmpty()){
      //TODO LOG
      return null;
    }

    final List<String> versions = extractListOfStringsFromParamValue(params, MetadataConstants.VERSIONS);
    if(versions.isEmpty()){
      //TODO LOG
      return null;
    }

    if(packageIds.size() != versions.size()){
      //TODO LOG
      return null;
    }

    final boolean includeAllVersions = extractBooleanParameterValue(params, MetadataConstants.INCLUDE_ALL_VERSIONS, false);
    final List<NuGetIndexEntry> result = new ArrayList<NuGetIndexEntry>();

    for(int i = 0; i < packageIds.size(); i++){
      final String version = versions.get(i);
      final String packageId = packageIds.get(i);
      final Iterator<NuGetIndexEntry> entryIterator = myIndex.getNuGetEntries(packageId);
      while (entryIterator.hasNext()){
        final NuGetIndexEntry indexEntry = entryIterator.next();
        if(SEMANTIC_VERSIONS_COMPARATOR.compare(version, indexEntry.getPackageInfo().getVersion()) < 0){
          //TODO LOG
          result.add(indexEntry);
          if(!includeAllVersions){
            //TODO LOG
            break;
          }
        }
      }
    }

    if(result.isEmpty()){
      //TODO LOG
      return null;
    }
    return new NuGetPackagesCollectionResponse(result.iterator(), returnType);
  }

  private boolean extractBooleanParameterValue(Map<String, OFunctionParameter> parameters, String parameterName, boolean defaultValue) {
    final OFunctionParameter parameter = parameters.get(parameterName);
    if(parameter == null){
      //TODO: LOG
      return defaultValue;
    }
    final OObject valueObject = parameter.getValue();
    if(!(valueObject instanceof OSimpleObject))
    {
      //TODO: LOG
      return defaultValue;
    }
    return Boolean.valueOf(((OSimpleObject) valueObject).getValue().toString());
  }

  private List<String> extractListOfStringsFromParamValue(Map<String, OFunctionParameter> parameters, String parameterName){
    final OFunctionParameter parameter = parameters.get(parameterName);
    if(parameter == null){
      //TODO: LOG
      return null;
    }
    final OObject valueObject = parameter.getValue();
    if(!(valueObject instanceof OSimpleObject))
    {
      //TODO: LOG
      return null;
    }
    return StringUtil.split(((OSimpleObject) valueObject).getValue().toString(), COLLECTION_VALUE_SEPARATOR);
  }
}
