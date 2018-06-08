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

package jetbrains.buildServer.nuget.feed.server.olingo.data;

import jetbrains.buildServer.nuget.feed.server.MetadataConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.util.StringUtil;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.exception.ODataHttpException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.uri.UriSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Controls request to the data source.
 */
public class OlingoDataSource {

  private final NuGetFeed myFeed;
  private final NuGetAPIVersion myApiVersion;
  private final Map<String, OlingoFeedFunction> myFunctions = new HashMap<>();

  public OlingoDataSource(@NotNull final NuGetFeed feed, @NotNull final NuGetAPIVersion apiVersion) {
    myFeed = feed;
    myApiVersion = apiVersion;

    myFunctions.put(MetadataConstants.SEARCH_FUNCTION_NAME, parameters -> {
      final String searchTerm = (String) parameters.get(MetadataConstants.SEARCH_TERM);
      final String targetFramework = (String) parameters.get(MetadataConstants.TARGET_FRAMEWORK);
      final boolean includePrerelease = myApiVersion == NuGetAPIVersion.V2 && getBooleanValue(parameters.get(MetadataConstants.INCLUDE_PRERELEASE));
      if (searchTerm == null || targetFramework == null) {
        throw new UriSyntaxException(UriSyntaxException.MISSINGPARAMETER);
      }

      return myFeed.search(searchTerm, targetFramework, includePrerelease);
    });

    myFunctions.put(MetadataConstants.FIND_PACKAGES_BY_ID_FUNCTION_NAME, parameters -> {
      final String id = (String) parameters.get(MetadataConstants.ID);
      if (id == null) {
        throw new UriSyntaxException(UriSyntaxException.MISSINGPARAMETER);
      }

      return myFeed.findPackagesById(id);
    });

    myFunctions.put(MetadataConstants.GET_UPDATES_FUNCTION_NAME, parameters -> myFeed.getUpdates(
      getStringValue(parameters.get(MetadataConstants.PACKAGE_IDS)),
      getStringValue(parameters.get(MetadataConstants.VERSIONS)),
      getStringValue(parameters.get(MetadataConstants.VERSION_CONSTRAINTS)),
      getStringValue(parameters.get(MetadataConstants.TARGET_FRAMEWORKS)),
      getBooleanValue(parameters.get(MetadataConstants.INCLUDE_PRERELEASE)),
      getBooleanValue(parameters.get(MetadataConstants.INCLUDE_ALL_VERSIONS))));
  }

  /**
   * Retrieves complete data set data.
   *
   * @param entitySet is a target entity set.
   * @return data
   */
  @NotNull
  public List<?> readData(@NotNull final EdmEntitySet entitySet) throws ODataHttpException, EdmException {
    if (MetadataConstants.ENTITY_SET_NAME.equals(entitySet.getName())) {
      return myFeed.getAll();
    }

    throw new ODataNotImplementedException();
  }

  /**
   * Retrieves entities with a limited set of properties.
   *
   * @param entitySet is a target entity set
   * @param keys      required proeprties.
   * @return data
   */
  @NotNull
  public Object readData(@NotNull final EdmEntitySet entitySet,
                         @NotNull final Map<String, Object> keys) throws ODataHttpException, EdmException {
    if (MetadataConstants.ENTITY_SET_NAME.equals(entitySet.getName())) {
      if (keys.isEmpty()) {
        throw new ODataNotImplementedException();
      }

      final Map<String, String> query = new HashMap<>(keys.size());
      for (String key : keys.keySet()) {
        query.put(key, (String) keys.get(key));
      }

      final List<NuGetIndexEntry> result = myFeed.find(query);
      if (result.size() > 0) {
        return result.get(0);
      } else {
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
      }
    }

    throw new ODataNotImplementedException();
  }

  /**
   * Retrieves data for function calls.
   *
   * @param function   is a function reference.
   * @param parameters is a list of parameters.
   * @param keys       is a list of keys to select.
   * @return data.
   */
  @NotNull
  public Object readData(@NotNull final EdmFunctionImport function,
                         @NotNull final Map<String, Object> parameters,
                         @Nullable final Map<String, Object> keys) throws ODataHttpException, EdmException {
    final OlingoFeedFunction handler = myFunctions.get(function.getName());
    if (handler == null) {
      throw new ODataNotImplementedException();
    }

    return handler.handle(parameters);
  }

  private interface OlingoFeedFunction {
    @NotNull
    Object handle(@NotNull final Map<String, Object> parameters) throws ODataHttpException;
  }

  private static String getStringValue(@Nullable Object value) {
    return StringUtil.notEmpty((String) value, StringUtil.EMPTY);
  }

  private static boolean getBooleanValue(@Nullable Object value) {
    return value != null ? (Boolean) value : false;
  }
}
