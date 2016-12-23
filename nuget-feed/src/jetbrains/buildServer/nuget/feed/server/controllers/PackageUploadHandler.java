package jetbrains.buildServer.nuget.feed.server.controllers;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCacheReset;
import jetbrains.buildServer.nuget.feed.server.index.PackageAnalyzer;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.serverSide.metadata.impl.MetadataStorageEx;
import jetbrains.buildServer.serverSide.metadata.impl.MetadataStorageException;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.*;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.MessageFormat;
import java.util.Map;

import static jetbrains.buildServer.nuget.feed.server.index.PackagesIndex.*;
import static jetbrains.buildServer.nuget.feed.server.index.impl.NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * NuGet package upload handler.
 */
public class PackageUploadHandler implements NuGetFeedHandler {

    private static final Logger LOG = Logger.getInstance(PackageUploadHandler.class.getName());
    private static final String INVALID_TOKEN_VALUE = "Invalid token value";
    private static final String INVALID_PACKAGE_CONTENTS = "Invalid NuGet package contents";
    private final RunningBuildsCollection myRunningBuilds;
    private final ServerSettings myServerSettings;
    private final MetadataStorageEx myStorage;
    private final PackageAnalyzer myPackageAnalyzer;
    private final ResponseCacheReset myCacheReset;

    public PackageUploadHandler(@NotNull final RunningBuildsCollection runningBuilds,
                                @NotNull final ServerSettings serverSettings,
                                @NotNull final MetadataStorageEx storage,
                                @NotNull final PackageAnalyzer packageAnalyzer,
                                @NotNull final ResponseCacheReset cacheReset) {
        myRunningBuilds = runningBuilds;
        myServerSettings = serverSettings;
        myStorage = storage;
        myPackageAnalyzer = packageAnalyzer;
        myCacheReset = cacheReset;
    }

    @Override
    public void handleRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        final String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request body should be multipart form data");
            return;
        }

        final MultipartResolver multipartResolver = new CommonsMultipartResolver();
        final MultipartHttpServletRequest servletRequest = multipartResolver.resolveMultipart(request);
        try {
            handleUpload(servletRequest, response);
        } finally {
            multipartResolver.cleanupMultipart(servletRequest);
        }
    }

    private void handleUpload(@NotNull final MultipartHttpServletRequest request,
                              @NotNull final HttpServletResponse response) throws IOException {
        final RunningBuildEx build = getRunningBuild(request.getHeader("x-nuget-apikey"));
        if (build == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, INVALID_TOKEN_VALUE);
            return;
        }

        final MultipartFile file = request.getFile("package");
        if (file == null) {
            LOG.debug("Push NuGet package request does not contain package file");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "NuGet package data not found");
            return;
        }

        if (file.getSize() > myServerSettings.getMaximumAllowedArtifactSize()) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "NuGet package is too large");
            return;
        }

        try {
            processPackage(build, file);
        } catch (PackageLoadException e) {
            LOG.debug("Invalid NuGet package: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_PACKAGE_CONTENTS);
        } catch (Throwable e) {
            LOG.warnAndDebugDetails("Failed to process NuGet package: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to process NuGet package");
        }
    }

    @Nullable
    private RunningBuildEx getRunningBuild(@Nullable final String tokenValue) throws IOException {
        if (StringUtil.isEmptyOrSpaces(tokenValue)) {
            return null;
        }

        final String token;
        try {
            token = EncryptUtil.unscramble(tokenValue);
        } catch (IllegalArgumentException e) {
            return null;
        }

        if (!token.startsWith(NuGetFeedConstants.BUILD_TOKEN_PREFIX)) {
            return null;
        }

        long buildId;
        try {
            buildId = Long.parseLong(token.substring(NuGetFeedConstants.BUILD_TOKEN_PREFIX.length()));
        } catch (NumberFormatException e) {
            return null;
        }

        final RunningBuildEx build = myRunningBuilds.findRunningBuildById(buildId);
        if (build == null) {
            LOG.debug(String.format("Running build %s not found", buildId));
            return null;
        }

        return build;
    }

    private void processPackage(@NotNull RunningBuildEx build,
                                @NotNull MultipartFile file) throws Exception {
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
        if (StringUtil.isEmptyOrSpaces(id) || StringUtil.isEmptyOrSpaces(version)) {
            throw new PackageLoadException("Lack of Id or Version in NuGet package specification");
        }

        final String path = MessageFormat.format(".teamcity/nuget/packages/{0}/{1}/{0}.{1}.nupkg", id, version);
        metadata.put(PACKAGE_SIZE, String.valueOf(file.getSize()));
        metadata.put(TEAMCITY_ARTIFACT_RELPATH, path);
        metadata.put(TEAMCITY_BUILD_TYPE_ID, build.getBuildTypeId());

        try {
            inputStream = file.getInputStream();
            metadata.put(PACKAGE_HASH, myPackageAnalyzer.getSha512Hash(inputStream));
            metadata.put(PACKAGE_HASH_ALGORITHM, PackageAnalyzer.SHA512);
        } finally {
            FileUtil.close(inputStream);
        }

        try {
            inputStream = file.getInputStream();
            build.publishArtifact(path, inputStream);
        } catch (IOException e) {
            LOG.warnAndDebugDetails(String.format("Failed to publish build %s artifact at the path %s: %s",
                    build.getBuildId(), path, e.getMessage()), e);
            throw e;
        } finally {
            FileUtil.close(inputStream);
        }

        try {
            myStorage.updateCache(build.getBuildId(), !build.isPersonal(), NUGET_PROVIDER_ID,
                    metadataStorageWriter -> metadataStorageWriter.addParameters(id, metadata));
        } catch (MetadataStorageException e) {
            LOG.warnAndDebugDetails(String.format("Failed to update %s provider metadata for build %s. Error: %s",
                    NUGET_PROVIDER_ID, build, e.getMessage()), e);
            throw e;
        }

        myCacheReset.resetCache();
    }
}
