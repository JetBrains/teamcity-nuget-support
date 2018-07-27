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

package jetbrains.buildServer.nuget.feed.server.odata4j.functions;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.feed.server.MetadataConstants;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.feed.server.odata4j.PackagesEntitySet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.QueryInfo;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class GetUpdatesFunction extends NuGetFeedFunctionBase {

  private final Logger LOG = Logger.getInstance(getClass().getName());
  private final NuGetFeed myFeed;

  public GetUpdatesFunction(@NotNull final NuGetFeed feed) {
    myFeed = feed;
  }

  @NotNull
  public String getName() {
    return MetadataConstants.GET_UPDATES_FUNCTION_NAME;
  }

  @NotNull
  public EdmFunctionImport.Builder generateImport(@NotNull EdmType returnType) {
    return new EdmFunctionImport.Builder()
      .setName(MetadataConstants.GET_UPDATES_FUNCTION_NAME)
      .setEntitySet(PackagesEntitySet.getBuilder())
      .setHttpMethod(MetadataConstants.HTTP_METHOD_GET)
      .setReturnType(returnType)
      .addParameters(new EdmFunctionParameter.Builder().setName(MetadataConstants.PACKAGE_IDS).setType(EdmSimpleType.STRING),
        new EdmFunctionParameter.Builder().setName(MetadataConstants.VERSIONS).setType(EdmSimpleType.STRING),
        new EdmFunctionParameter.Builder().setName(MetadataConstants.INCLUDE_PRERELEASE).setType(EdmSimpleType.BOOLEAN),
        new EdmFunctionParameter.Builder().setName(MetadataConstants.INCLUDE_ALL_VERSIONS).setType(EdmSimpleType.BOOLEAN),
        new EdmFunctionParameter.Builder().setName(MetadataConstants.TARGET_FRAMEWORKS).setType(EdmSimpleType.STRING),
        new EdmFunctionParameter.Builder().setName(MetadataConstants.VERSION_CONSTRAINTS).setType(EdmSimpleType.STRING),
        new EdmFunctionParameter.Builder().setName(MetadataConstants.SEMANTIC_VERSION).setType(EdmSimpleType.STRING));
  }

  @Nullable
  public Iterable<NuGetIndexEntry> call(@NotNull EdmType returnType, @NotNull Map<String, OFunctionParameter> params, @Nullable QueryInfo queryInfo) {
    final boolean includeSemVer2 = includeSemVer2(params);
    return myFeed.getUpdates(
      extractStringParameterValue(params, MetadataConstants.PACKAGE_IDS),
      extractStringParameterValue(params, MetadataConstants.VERSIONS),
      extractStringParameterValue(params, MetadataConstants.VERSION_CONSTRAINTS),
      extractStringParameterValue(params, MetadataConstants.TARGET_FRAMEWORKS),
      extractBooleanParameterValue(params, MetadataConstants.INCLUDE_PRERELEASE),
      extractBooleanParameterValue(params, MetadataConstants.INCLUDE_ALL_VERSIONS),
      includeSemVer2
    );
  }

  private boolean extractBooleanParameterValue(Map<String, OFunctionParameter> parameters, String parameterName) {
    return Boolean.valueOf(extractStringParameterValue(parameters, parameterName));
  }

  @NotNull
  private String extractStringParameterValue(Map<String, OFunctionParameter> parameters, String parameterName) {
    final OFunctionParameter parameter = parameters.get(parameterName);
    if (parameter == null) {
      LOG.debug(String.format("Bad %s function call. %s parameter is not specified.", getName(), parameterName));
      return "";
    }
    final OObject valueObject = parameter.getValue();
    if (!(valueObject instanceof OSimpleObject)) {
      LOG.debug(String.format("Bad %s function call. %s parameter type is invalid.", getName(), parameterName));
      return "";
    }
    return ((OSimpleObject) valueObject).getValue().toString();
  }
}
