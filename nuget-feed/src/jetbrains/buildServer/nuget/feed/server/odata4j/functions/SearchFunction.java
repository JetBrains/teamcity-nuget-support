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

package jetbrains.buildServer.nuget.feed.server.odata4j.functions;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.feed.server.odata4j.PackageEntityEx;
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes;
import jetbrains.buildServer.nuget.server.version.FrameworkConstraints;
import jetbrains.buildServer.nuget.server.version.VersionUtility;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.*;
import org.odata4j.exceptions.BadRequestException;
import org.odata4j.producer.QueryInfo;

import java.util.*;

import static jetbrains.buildServer.nuget.feed.server.MetadataConstants.*;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.IS_PRERELEASE;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.VERSION;

/**
 * @author Evgeniy.Koshkin
 */
public class SearchFunction implements NuGetFeedFunction {
  private static final Logger LOG = Logger.getInstance(SearchFunction.class.getName());

  @NotNull private final PackagesIndex myIndex;
  @NotNull private final NuGetServerSettings myServerSettings;

  public SearchFunction(@NotNull PackagesIndex index, @NotNull NuGetServerSettings serverSettings) {
    myIndex = index;
    myServerSettings = serverSettings;
  }

  @NotNull
  public String getName() {
    return SEARCH_FUNCTION_NAME;
  }

  @NotNull
  public EdmFunctionImport.Builder generateImport(@NotNull EdmType returnType) {
    final EdmEntitySet.Builder packagesEntitySet = new EdmEntitySet.Builder().setName(ENTITY_SET_NAME);
    return new EdmFunctionImport.Builder()
            .setName(SEARCH_FUNCTION_NAME)
            .setEntitySet(packagesEntitySet)
            .setHttpMethod(HTTP_METHOD_GET)
            .setReturnType(returnType)
            .addParameters(new EdmFunctionParameter.Builder().setName(SEARCH_TERM).setType(EdmSimpleType.STRING),
                    new EdmFunctionParameter.Builder().setName(TARGET_FRAMEWORK).setType(EdmSimpleType.STRING),
                    new EdmFunctionParameter.Builder().setName(INCLUDE_PRERELEASE).setType(EdmSimpleType.BOOLEAN));
  }

  @Nullable
  public Iterable<Object> call(@NotNull EdmType returnType, @NotNull Map<String, OFunctionParameter> params, @Nullable QueryInfo queryInfo) {
    final OFunctionParameter searchTermParam = params.get(SEARCH_TERM);
    if(searchTermParam == null){
      LOG.debug(String.format("Bad %s function call. searchTerm parameter is not specified.", getName()));
      throw new BadRequestException(SEARCH_TERM + " parameter is not specified.");
    }
    final OObject searchTermParamValue = searchTermParam.getValue();
    if(!(searchTermParamValue instanceof OSimpleObject))
    {
      LOG.debug(String.format("Bad %s function call. searchTerm parameter type is invalid.", getName()));
      throw new BadRequestException(SEARCH_TERM + " parameter type is invalid.");
    }
    final OSimpleObject searchTermCasted = (OSimpleObject) searchTermParamValue;
    final String searchTerm = searchTermCasted.getValue().toString();

    final OFunctionParameter targetFrameworkParam = params.get(TARGET_FRAMEWORK);
    if(targetFrameworkParam == null){
      LOG.debug(String.format("Bad %s function call. targetFramework parameter is not specified.", getName()));
      throw new BadRequestException(TARGET_FRAMEWORK + " parameter is not specified.");
    }
    final OObject targetFrameworkParamValue = targetFrameworkParam.getValue();
    if(!(targetFrameworkParamValue instanceof OSimpleObject))
    {
      LOG.debug(String.format("Bad %s function call. targetFramework parameter type is invalid.", getName()));
      throw new BadRequestException(TARGET_FRAMEWORK + " parameter type is invalid.");
    }
    final OSimpleObject targetFrameworkCasted = (OSimpleObject) targetFrameworkParamValue;
    final String requestedTargetFramework = targetFrameworkCasted.getValue().toString();
    final Set<String> requestedFrameworkConstraints = FrameworkConstraints.convertFromString(requestedTargetFramework);

    final OFunctionParameter includePreReleaseParam = params.get(INCLUDE_PRERELEASE);
    if(includePreReleaseParam == null){
      LOG.debug(String.format("Bad %s function call. 'includePrerelease' parameter is not specified.", getName()));
      throw new BadRequestException(INCLUDE_PRERELEASE + " parameter is not specified.");
    }
    final OObject includePreReleaseParamValue = includePreReleaseParam.getValue();
    if(!(includePreReleaseParamValue instanceof OSimpleObject))
    {
      LOG.debug(String.format("Bad %s function call. 'includePrerelease' parameter type is invalid.", getName()));
      throw new BadRequestException(INCLUDE_PRERELEASE + " parameter type is invalid.");
    }
    final OSimpleObject includePreReleaseCasted = (OSimpleObject) includePreReleaseParamValue;
    final boolean includePreRelease = Boolean.parseBoolean(includePreReleaseCasted.getValue().toString());

    String format = String.format("Searching packages by term '%s'", searchTerm);
    if(includePreRelease) format += ", including pre-release packages";
    if(!requestedTargetFramework.isEmpty()) format += ", with target framework constraints " + requestedTargetFramework;
    LOG.debug(format);

    final List<NuGetIndexEntry> result = new ArrayList<>();
    final Iterator<NuGetIndexEntry> entryIterator = myIndex.search(searchTerm);
    while (entryIterator.hasNext()){
      final NuGetIndexEntry nugetPackage = entryIterator.next();
      final Map<String, String> nugetPackageAttributes = nugetPackage.getAttributes();
      final String id = nugetPackageAttributes.get(NuGetPackageAttributes.ID);
      final String version = nugetPackageAttributes.get(VERSION);
      final String isPrerelease = nugetPackageAttributes.get(IS_PRERELEASE);
      final String frameworkConstraints = nugetPackageAttributes.get(PackagesIndex.TEAMCITY_FRAMEWORK_CONSTRAINTS);
      if(!includePreRelease && Boolean.parseBoolean(isPrerelease)){
        if(LOG.isDebugEnabled()) LOG.debug(String.format("Skipped package (id:%s, version:%s) since its pre-released.", id, version));
        continue;
      }
      if(myServerSettings.isFilteringByTargetFrameworkEnabled()){
        final Set<String> packageFrameworkConstraints = FrameworkConstraints.convertFromString(frameworkConstraints);
        if (!VersionUtility.isPackageCompatibleWithFrameworks(requestedFrameworkConstraints, packageFrameworkConstraints)) {
          if(LOG.isDebugEnabled())
            LOG.debug(String.format("Skipped package (id:%s, version:%s) since it doesn't match requested framework constraints.", id, version));
          continue;
        }
      }
      result.add(nugetPackage);
    }

    LOG.debug(String.format("Found %d package(s) while " + format, result.size()));
    return CollectionsUtil.convertCollection(result, source -> new PackageEntityEx(source, myServerSettings));
  }
}
