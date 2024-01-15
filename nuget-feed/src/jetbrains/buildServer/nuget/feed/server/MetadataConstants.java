

package jetbrains.buildServer.nuget.feed.server;

/**
 * @author Evgeniy.Koshkin
 */
public class MetadataConstants {
  public static final String NUGET_GALLERY_NAMESPACE = "NuGetGallery";
  public static final String ENTITY_NAMESPACE = "NuGetGallery.OData";
  public static final String ENTITY_SET_NAME = "Packages";
  public static final String ENTITY_TYPE_NAME = "FeedPackage";
  public static final String CONTAINER_NAME = "V2FeedContext";
  public static final String HTTP_METHOD_GET = "GET";
  public static final String SEARCH_FUNCTION_NAME = "Search";
  public static final String FIND_PACKAGES_BY_ID_FUNCTION_NAME = "FindPackagesById";
  public static final String GET_UPDATES_FUNCTION_NAME = "GetUpdates";

  public static final String ID = "id";
  public static final String ID_UPPER_CASE = "Id";  //used by VS since 2015 version
  public static final String SEARCH_TERM = "searchTerm";
  public static final String SEMANTIC_VERSION = "semVerLevel";
  public static final String TARGET_FRAMEWORK = "targetFramework";
  public static final String INCLUDE_PRERELEASE = "includePrerelease";
  public static final String PACKAGE_IDS = "packageIds";
  public static final String VERSIONS = "versions";
  public static final String INCLUDE_ALL_VERSIONS = "includeAllVersions";
  public static final String TARGET_FRAMEWORKS = "targetFrameworks";
  public static final String VERSION_CONSTRAINTS = "versionConstraints";

  public static final String CONTENT_TYPE = "ContentType";
}
