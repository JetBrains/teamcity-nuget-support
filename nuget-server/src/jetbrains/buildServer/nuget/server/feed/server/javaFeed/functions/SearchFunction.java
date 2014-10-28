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
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.server.feed.server.PackageAttributes;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.PackageEntityEx;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.*;
import org.odata4j.producer.QueryInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.nuget.server.feed.server.PackageAttributes.*;
import static jetbrains.buildServer.nuget.server.feed.server.javaFeed.MetadataConstants.*;

/**
 * @author Evgeniy.Koshkin
 */
public class SearchFunction implements NuGetFeedFunction {

  private static final Logger LOG = Logger.getInstance(SearchFunction.class.getName());
  private static final String[] PACKAGE_ATTRIBUTES_TO_SEARCH = new String[]{ PackageAttributes.ID, /*TITLE,*/ TAGS, DESCRIPTION, AUTHORS, /* OWNERS */};

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
      return null;
    }
    final OObject searchTermParamValue = searchTermParam.getValue();
    if(!(searchTermParamValue instanceof OSimpleObject))
    {
      LOG.debug(String.format("Bad %s function call. searchTerm parameter type is invalid.", getName()));
      return null;
    }
    final OSimpleObject searchTermCasted = (OSimpleObject) searchTermParamValue;
    final String searchTerm = searchTermCasted.getValue().toString();

    final OFunctionParameter includePreReleaseParam = params.get(INCLUDE_PRERELEASE);
    if(includePreReleaseParam == null){
      LOG.debug(String.format("Bad %s function call. 'includePrerelease' parameter is not specified.", getName()));
      return null;
    }
    final OObject includePreReleaseParamValue = includePreReleaseParam.getValue();
    if(!(includePreReleaseParamValue instanceof OSimpleObject))
    {
      LOG.debug(String.format("Bad %s function call. 'includePrerelease' parameter type is invalid.", getName()));
      return null;
    }
    final OSimpleObject includePreReleaseCasted = (OSimpleObject) includePreReleaseParamValue;
    final boolean includePreRelease = Boolean.parseBoolean(includePreReleaseCasted.getValue().toString());

    String format = String.format("Searching packages by term %s", searchTerm);
    if(includePreRelease) format += ", including pre-release packages";
    LOG.debug(format);

    final Iterator<NuGetIndexEntry> entryIterator = myIndex.getNuGetEntries();
    final List<NuGetIndexEntry> result = new ArrayList<NuGetIndexEntry>();
    while (entryIterator.hasNext()){
      final NuGetIndexEntry indexEntry = entryIterator.next();
      if(matches(indexEntry, searchTerm, includePreRelease)){
        result.add(indexEntry);
      }
    }

    if(result.isEmpty()){
      LOG.debug(String.format("No packages found by searchTerm %s.", searchTerm));
      return null;
    }

    return CollectionsUtil.convertCollection(result, new Converter<Object, NuGetIndexEntry>() {
      public Object createFrom(@NotNull NuGetIndexEntry source) {
        return new PackageEntityEx(source, myServerSettings);
      }
    });
  }

  private boolean matches(NuGetIndexEntry indexEntry, String searchTerm, boolean includePreRelease) {
    final Map<String, String> indexEntryAttributes = indexEntry.getAttributes();
    if(!includePreRelease && Boolean.parseBoolean(indexEntryAttributes.get(IS_PRERELEASE))) return false;
    for(String attributeName : PACKAGE_ATTRIBUTES_TO_SEARCH){
      final String attributeValue = indexEntryAttributes.get(attributeName);
      if(attributeValue != null && attributeValue.contains(searchTerm))
        return true;
    }
    return false;
  }
}
