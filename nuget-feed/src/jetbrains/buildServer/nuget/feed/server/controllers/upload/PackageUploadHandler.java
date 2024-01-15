

package jetbrains.buildServer.nuget.feed.server.controllers.upload;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.BuildAuthUtil;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.log.LogUtil;
import jetbrains.buildServer.nuget.common.PackageExistsException;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetUtils;
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCacheReset;
import jetbrains.buildServer.nuget.common.index.PackageAnalyzer;
import jetbrains.buildServer.nuget.common.index.ODataDataFormat;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexUtils;
import jetbrains.buildServer.serverSide.RunningBuildEx;
import jetbrains.buildServer.serverSide.RunningBuildsCollection;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.artifacts.limits.ArtifactsUploadLimit;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.nuget.common.index.PackageConstants.*;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * NuGet package upload handler.
 */
public class PackageUploadHandler<TContext extends NuGetFeedUploadHandlerContext> implements
                                                                                  NuGetFeedUploadHandler<TContext> {

  private static final Logger LOG = Logger.getInstance(PackageUploadHandler.class.getName());
  private static final String INVALID_TOKEN_VALUE = "Invalid token value";
  private static final String INVALID_PACKAGE_CONTENTS = "Invalid NuGet package contents";
  private static final String ARTIFACT_PUBLISHING_FAILED = "[Artifacts publishing failed]";
  private static final String DEFAULT_PATH_FORMAT = "nuget/packages/{0}/{1}/{0}.{1}.nupkg";
  public static final String NUGET_APIKEY_HEADER = "x-nuget-apikey";
  private final RunningBuildsCollection myRunningBuilds;
  private final PackageAnalyzer myPackageAnalyzer;
  private final ResponseCacheReset myCacheReset;
  private final ServerSettings myServerSettings;
  private final NuGetFeedUploadMetadataHandler<TContext> myMetadataHandler;

  public PackageUploadHandler(@NotNull final RunningBuildsCollection runningBuilds,
                              @NotNull final PackageAnalyzer packageAnalyzer,
                              @NotNull final ResponseCacheReset cacheReset,
                              @NotNull final ServerSettings serverSettings,
                              @NotNull final NuGetFeedUploadMetadataHandler<TContext> metadataHandler) {
    myRunningBuilds = runningBuilds;
    myPackageAnalyzer = packageAnalyzer;
    myCacheReset = cacheReset;
    myServerSettings = serverSettings;
    myMetadataHandler = metadataHandler;
  }

  @Override
  public void handleRequest(@NotNull final TContext context,
                            @NotNull final HttpServletRequest request,
                            @NotNull final HttpServletResponse response) throws Exception {
    final String contentType = request.getContentType();
    if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
      LOG.debug("Request body should be multipart form data");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request body should be multipart form data");
      return;
    }

    final CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    final File uploadDirectory = getUploadDirectory();
    multipartResolver.setUploadTempDir(new FileSystemResource(uploadDirectory));
    MultipartHttpServletRequest servletRequest = null;
    try {
      LOG.debug("NuGet package upload handler has started");
      servletRequest = multipartResolver.resolveMultipart(request);
      handleUpload(servletRequest, response, context);
    } catch (Throwable e) {
      LOG.warnAndDebugDetails("Unhandled error while processing NuGet package upload: " + e.getMessage(), e);
      throw e;
    } finally {
      LOG.debug("NuGet package upload handler has finished");
      if (servletRequest != null) {
        multipartResolver.cleanupMultipart(servletRequest);
      }
    }
  }

  private void handleUpload(final MultipartHttpServletRequest request,
                            final HttpServletResponse response,
                            final TContext context) throws IOException {
    final RunningBuildEx build = getRunningBuild(request.getHeader(NUGET_APIKEY_HEADER));
    if (build == null) {
      LOG.debug(INVALID_TOKEN_VALUE);
      response.sendError(HttpServletResponse.SC_FORBIDDEN, INVALID_TOKEN_VALUE);
      return;
    }

    if (!NuGetIndexUtils.isIndexingEnabledForBuild(build)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Indexing is disabled for build " + LogUtil.describe(build));
      return;
    }

    final MultipartFile file = request.getFile("package");
    if (file == null) {
      LOG.debug("Push NuGet package request does not contain package file");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "NuGet package data not found");
      return;
    }

    // Check maximum size of artifacts
    final ArtifactsUploadLimit artifactsLimit = build.getArtifactsLimit();
    long fileSize = file.getSize();

    final Long maxArtifactFileSize = artifactsLimit.getMaxArtifactFileSize();
    if (maxArtifactFileSize != null && maxArtifactFileSize >= 0 && fileSize > maxArtifactFileSize) {
      final String message = String.format(
        "NuGet package size is %s bytes which exceeds maximum build artifact file size of %s bytes.\n" +
          "Consider increasing this limit on the Administration -> Global Settings page.",
        fileSize, maxArtifactFileSize);
      BuildProblemData problem = BuildProblemData.createBuildProblem(
        ARTIFACT_PUBLISHING_FAILED + "_maxArtifactFileSize",
        ARTIFACT_PUBLISHING_FAILED,
        message);
      build.addBuildProblem(problem);
      LOG.debug(message);
      response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "NuGet package is too large");
      return;
    }

    final Long totalSizeLimit = artifactsLimit.getArtifactsTotalSizeLimit();
    if (totalSizeLimit != null && totalSizeLimit >= 0 && fileSize > totalSizeLimit) {
      final String message = String.format(
        "NuGet package size is %s bytes which exceeds elapsed size of build configuration artifacts of %s bytes.\n" +
          "Consider increasing this limit in the project or build configuration parameters.",
        fileSize, totalSizeLimit);
      BuildProblemData problem = BuildProblemData.createBuildProblem(
        ARTIFACT_PUBLISHING_FAILED + "_totalSizeLimit",
        ARTIFACT_PUBLISHING_FAILED,
        message);
      build.addBuildProblem(problem);
      LOG.debug(message);
      response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "NuGet package is too large");
      return;
    }

    try {
      processPackage(request, response, build, file, context);
    } catch (PackageLoadException e) {
      LOG.debug("Invalid NuGet package: " + e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_PACKAGE_CONTENTS);
    } catch (PackageExistsException e) {
      LOG.debug(e);
      response.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
    } catch (Throwable e) {
      LOG.warnAndDebugDetails("Failed to process NuGet package: " + e.getMessage(), e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to process NuGet package");
    }
  }

  @Nullable
  private RunningBuildEx getRunningBuild(@Nullable final String tokenValue) {
    if (StringUtil.isEmptyOrSpaces(tokenValue)) {
      return null;
    }

    final String token;
    try {
      token = EncryptUtil.unscramble(tokenValue);
    } catch (IllegalArgumentException e) {
      return null;
    }

    final List<String> parts = StringUtil.split(token, true, ':');
    if (parts.size() != 2) {
      return null;
    }

    long buildId = BuildAuthUtil.getBuildId(parts.get(0));
    if (buildId < 0) {
      return null;
    }

    final RunningBuildEx build = myRunningBuilds.findRunningBuildById(buildId);
    if (build == null) {
      LOG.debug(String.format("Running build %s not found", buildId));
      return null;
    }

    if (!build.getAgentAccessCode().equals(parts.get(1))) {
      LOG.info("Invalid access code for running build " + buildId);
      return null;
    }

    return build;
  }

  private void processPackage(@NotNull final MultipartHttpServletRequest request,
                              @NotNull final HttpServletResponse response,
                              @NotNull final RunningBuildEx build,
                              @NotNull final MultipartFile file,
                              @NotNull final TContext context) throws Exception {
    final Map<String, String> metadata;
    InputStream inputStream = null;

    try {
      inputStream = file.getInputStream();
      metadata = myPackageAnalyzer.analyzePackage(inputStream);
    } finally {
      FileUtil.close(inputStream);
    }

    final String id = metadata.get(ID);
    final String version = metadata.get(NORMALIZED_VERSION);

    // Package must have id and version specified
    if (StringUtil.isEmptyOrSpaces(id) || StringUtil.isEmptyOrSpaces(version)) {
      throw new PackageLoadException("Lack of Id or Version in NuGet package specification");
    }

    final String key = NuGetUtils.getPackageKey(id, version);

    myMetadataHandler.validate(request, response, context, build, key, metadata);

    String path;
    try {
      path = MessageFormat.format(getPathFormat(build), id, version);
    } catch (IllegalArgumentException e) {
      LOG.warn(String.format("Invalid '%s' parameter value: %s", NuGetFeedConstants.PROP_NUGET_FEED_PUBLISH_PATH, e.getMessage()));
      path = MessageFormat.format(DEFAULT_PATH_FORMAT, id, version);
    }

    metadata.put(PACKAGE_SIZE, String.valueOf(file.getSize()));
    metadata.put(TEAMCITY_ARTIFACT_RELPATH, path);
    metadata.put(TEAMCITY_BUILD_TYPE_ID, build.getBuildTypeId());

    final String created = ODataDataFormat.formatDate(new Date());
    metadata.put(CREATED, created);
    metadata.put(LAST_UPDATED, created);
    metadata.put(PUBLISHED, created);

    try {
      inputStream = file.getInputStream();
      metadata.put(PACKAGE_HASH, myPackageAnalyzer.getSha512Hash(inputStream));
      metadata.put(PACKAGE_HASH_ALGORITHM, PackageAnalyzer.SHA512);
    } finally {
      FileUtil.close(inputStream);
    }

    LOG.info(String.format("Publishing nuget package %s:%s at path '%s' as artifact for build %s into feed %s",
      id, version, path, LogUtil.describe(build), context.getFeedName()));
    try {
      inputStream = file.getInputStream();
      build.publishArtifact(path, inputStream);
    } catch (IOException e) {
      LOG.warnAndDebugDetails(String.format("Failed to publish build %s artifact at the path %s: %s",
        build.getBuildId(), path, e.getMessage()), e);
      throw e;
    } finally {
      LOG.debug(String.format("Published nuget package %s:%s as artifact for build %s",
        id, version, LogUtil.describe(build)));
      FileUtil.close(inputStream);
    }

    myMetadataHandler.handleMetadata(request, response, context, build, key, metadata);

    myCacheReset.resetCache();
  }

  @NotNull
  private static String getPathFormat(RunningBuildEx build) {
    final String pathParameter = build.getBuildOwnParameters().get(NuGetFeedConstants.PROP_NUGET_FEED_PUBLISH_PATH);
    if (StringUtil.isEmptyOrSpaces(pathParameter)) {
      return TeamCityProperties.getProperty(NuGetFeedConstants.PROP_NUGET_FEED_PUBLISH_PATH, DEFAULT_PATH_FORMAT).trim();
    } else {
      return pathParameter;
    }
  }

  @NotNull
  private File getUploadDirectory() {
    final String serverUrl = myServerSettings.getRootUrl();
    File dir = new File(myServerSettings.getArtifactDirectories().get(0), "_upload_" + FileUtil.fixDirectoryName(serverUrl));
    final String uploadDir = TeamCityProperties.getPropertyOrNull("teamcity.server.artifacts.uploadDir");
    if (uploadDir != null) {
      dir = new File(uploadDir);
    }

    return dir;
  }
}
