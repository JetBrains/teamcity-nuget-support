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
import org.springframework.web.multipart.*;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
        final String tokenValue = request.getHeader("x-nuget-apikey");
        if (StringUtil.isEmptyOrSpaces(tokenValue)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, INVALID_TOKEN_VALUE);
            return;
        }

        final String token;
        try {
            token = EncryptUtil.unscramble(tokenValue);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, INVALID_TOKEN_VALUE);
            return;
        }

        if (!token.startsWith(NuGetFeedConstants.BUILD_TOKEN_PREFIX)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, INVALID_TOKEN_VALUE);
            return;
        }

        long buildId;
        try {
            buildId = Long.parseLong(token.substring(NuGetFeedConstants.BUILD_TOKEN_PREFIX.length()));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, INVALID_TOKEN_VALUE);
            return;
        }

        final RunningBuildEx build = myRunningBuilds.findRunningBuildById(buildId);
        if (build == null) {
            LOG.debug(String.format("Running build %s not found", buildId));
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "NuGet package is too large");
            return;
        }

        final Map<String, String> metadata;
        InputStream inputStream = null;

        try {
            inputStream = file.getInputStream();
            metadata = myPackageAnalyzer.analyzePackage(inputStream);
        } catch (PackageLoadException e) {
            LOG.warnAndDebugDetails("Failed to read NuGet package content: " + e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_PACKAGE_CONTENTS);
            return;
        } finally {
            FileUtil.close(inputStream);
        }

        final String id = metadata.get(ID);
        final String version = metadata.get(NORMALIZED_VERSION);
        if (StringUtil.isEmptyOrSpaces(id) || StringUtil.isEmptyOrSpaces(version)) {
            LOG.debug("Lack of Id or Version in NuGet package specification");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid NuGet package contents");
            return;
        }

        final String path = MessageFormat.format(".teamcity/nuget/packages/{0}/{1}/{0}.{1}.nupkg", id, version);
        metadata.put(PACKAGE_SIZE, String.valueOf(file.getSize()));
        metadata.put(TEAMCITY_ARTIFACT_RELPATH, path);
        metadata.put(TEAMCITY_BUILD_TYPE_ID, build.getBuildTypeId());

        build.publishArtifact(path, file.getInputStream());

        try {
            myStorage.updateCache(build.getBuildId(), !build.isPersonal(), NUGET_PROVIDER_ID,
                    metadataStorageWriter -> metadataStorageWriter.addParameters(id, metadata));
        } catch (MetadataStorageException e) {
            LOG.warnAndDebugDetails(String.format("Failed to update %s provider metadata for build %s. Error: %s",
                    NUGET_PROVIDER_ID, build, e.getMessage()), e);
        }

        myCacheReset.resetCache();
    }
}
