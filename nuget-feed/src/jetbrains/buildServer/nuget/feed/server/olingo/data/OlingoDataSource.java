

package jetbrains.buildServer.nuget.feed.server.olingo.data;

import jetbrains.buildServer.nuget.common.version.SemanticVersion;
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
  private final SemanticVersion VERSION_20 = Objects.requireNonNull(SemanticVersion.valueOf("2.0.0"));

  public OlingoDataSource(@NotNull final NuGetFeed feed, @NotNull final NuGetAPIVersion apiVersion) {
    myFeed = feed;
    myApiVersion = apiVersion;

    myFunctions.put(MetadataConstants.SEARCH_FUNCTION_NAME, (parameters, queryParams) -> {
      final String searchTerm = (String) parameters.get(MetadataConstants.SEARCH_TERM);
      final String targetFramework = (String) parameters.get(MetadataConstants.TARGET_FRAMEWORK);
      final boolean includePrerelease = myApiVersion == NuGetAPIVersion.V2 && getBooleanValue(parameters.get(MetadataConstants.INCLUDE_PRERELEASE));
      if (searchTerm == null || targetFramework == null) {
        throw new UriSyntaxException(UriSyntaxException.MISSINGPARAMETER);
      }

      final boolean includeSemVer2 = includeSemVer2(queryParams);
      return myFeed.search(searchTerm, targetFramework, includePrerelease, includeSemVer2);
    });

    myFunctions.put(MetadataConstants.FIND_PACKAGES_BY_ID_FUNCTION_NAME, (parameters, queryParams) -> {
      final String id = (String) parameters.get(MetadataConstants.ID);
      if (id == null) {
        throw new UriSyntaxException(UriSyntaxException.MISSINGPARAMETER);
      }

      final boolean includeSemVer2 = includeSemVer2(queryParams);
      return myFeed.findPackagesById(id, includeSemVer2);
    });

    myFunctions.put(MetadataConstants.GET_UPDATES_FUNCTION_NAME, (parameters, queryParams) -> {
      final boolean includeSemVer2 = includeSemVer2(queryParams);
      return myFeed.getUpdates(
        getStringValue(parameters.get(MetadataConstants.PACKAGE_IDS)),
        getStringValue(parameters.get(MetadataConstants.VERSIONS)),
        getStringValue(parameters.get(MetadataConstants.VERSION_CONSTRAINTS)),
        getStringValue(parameters.get(MetadataConstants.TARGET_FRAMEWORKS)),
        getBooleanValue(parameters.get(MetadataConstants.INCLUDE_PRERELEASE)),
        getBooleanValue(parameters.get(MetadataConstants.INCLUDE_ALL_VERSIONS)),
        includeSemVer2);
    });
  }

  /**
   * Retrieves complete data set data.
   *
   * @param entitySet is a target entity set.
   * @return data
   */
  @NotNull
  public List<?> readAllData(@NotNull final EdmEntitySet entitySet,
                             @NotNull final Map<String, String> parameters) throws ODataHttpException, EdmException {
    if (MetadataConstants.ENTITY_SET_NAME.equals(entitySet.getName())) {
      final boolean includeSemVer2 = includeSemVer2(parameters);
      return myFeed.getAll(includeSemVer2);
    }

    throw new ODataNotImplementedException();
  }

  /**
   * Retrieves entities with a limited set of properties.
   *
   * @param entitySet is a target entity set
   * @param keys      required properties.
   * @return data
   */
  @NotNull
  public Object readDataWithKeys(@NotNull final EdmEntitySet entitySet,
                                 @NotNull final Map<String, Object> keys,
                                 @NotNull final Map<String, String> parameters) throws ODataHttpException, EdmException {
    if (MetadataConstants.ENTITY_SET_NAME.equals(entitySet.getName())) {
      if (keys.isEmpty()) {
        throw new ODataNotImplementedException();
      }

      final Map<String, String> query = new HashMap<>(keys.size());
      for (String key : keys.keySet()) {
        query.put(key, (String) keys.get(key));
      }

      final List<NuGetIndexEntry> result = myFeed.find(query, true);
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
   * @param queryParams is a list of query parameters.
   * @return data.
   */
  @NotNull
  public Object executeFunction(@NotNull final EdmFunctionImport function,
                                @NotNull final Map<String, Object> parameters,
                                @NotNull final Map<String, String> queryParams) throws ODataHttpException, EdmException {
    final OlingoFeedFunction handler = myFunctions.get(function.getName());
    if (handler == null) {
      throw new ODataNotImplementedException();
    }

    return handler.handle(parameters, queryParams);
  }

  private interface OlingoFeedFunction {
    @NotNull
    Object handle(@NotNull final Map<String, Object> parameters, @NotNull Map<String, String> queryParams) throws ODataHttpException;
  }

  private static String getStringValue(@Nullable Object value) {
    return StringUtil.notEmpty((String) value, StringUtil.EMPTY);
  }

  private static boolean getBooleanValue(@Nullable Object value) {
    return value != null ? (Boolean) value : false;
  }

  private boolean includeSemVer2(final Map<String, String> parameters) {
    final Map<String, String> queryParams = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    queryParams.putAll(parameters);

    final String semVerLevel = queryParams.get(MetadataConstants.SEMANTIC_VERSION);
    if (semVerLevel != null) {
      final SemanticVersion version = SemanticVersion.valueOf(semVerLevel);
      return version != null && version.compareTo(VERSION_20) >= 0;
    }
    return false;
  }
}
