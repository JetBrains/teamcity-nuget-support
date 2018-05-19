package jetbrains.buildServer.nuget.common.index;

import jetbrains.buildServer.ArtifactsConstants;

public class PackageConstants {
    public static final String TEAMCITY_ARTIFACT_RELPATH = "teamcity.artifactPath";
    public static final String TEAMCITY_FRAMEWORK_CONSTRAINTS = "teamcity.frameworkConstraints";
    public static final String TEAMCITY_BUILD_TYPE_ID = "teamcity.buildTypeId";
    public static final String TEAMCITY_BUILD_ID = "TeamCityBuildId";
    public static final String TEAMCITY_DOWNLOAD_URL = "TeamCityDownloadUrl";

    /**
     * The name TeamCity NuGet data provider.
     */
    public static final String NUGET_PROVIDER_ID = "nuget";

    /**
     * The name of TeamCity NuGet packages metadata file.
     */
    public static final String PACKAGES_LIST_NAME = "packages.json";

    /**
     * The relative path to NuGet packages metadata file.
     */
    public static final String PACKAGES_FILE_PATH = ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR + "/" +
            NUGET_PROVIDER_ID + "/" + PACKAGES_LIST_NAME;
}
