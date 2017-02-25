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

package jetbrains.buildServer.nuget.feed.server.controllers;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCacheReset;
import jetbrains.buildServer.nuget.feed.server.index.PackageAnalyzer;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.artifacts.limits.ArtifactsUploadLimit;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.buildLog.MessageAttrs;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
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
    private final MetadataStorage myStorage;
    private final PackageAnalyzer myPackageAnalyzer;
    private final ResponseCacheReset myCacheReset;

    public PackageUploadHandler(@NotNull final RunningBuildsCollection runningBuilds,
                                @NotNull final MetadataStorage storage,
                                @NotNull final PackageAnalyzer packageAnalyzer,
                                @NotNull final ResponseCacheReset cacheReset) {
        myRunningBuilds = runningBuilds;
        myStorage = storage;
        myPackageAnalyzer = packageAnalyzer;
        myCacheReset = cacheReset;
    }

    @Override
    public void handleRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        final String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
            LOG.debug("Request body should be multipart form data");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request body should be multipart form data");
            return;
        }

        final MultipartResolver multipartResolver = new CommonsMultipartResolver();
        MultipartHttpServletRequest servletRequest = null;
        try {
            LOG.debug("NuGet package upload handler started");
            servletRequest = multipartResolver.resolveMultipart(request);
            handleUpload(servletRequest, response);
        } catch (Throwable e) {
            LOG.warnAndDebugDetails("Unhandled error while processing NuGet package upload: " + e.getMessage(), e);
            throw e;
        } finally {
            LOG.debug("NuGet package upload handler finished");
            if (servletRequest != null) {
                multipartResolver.cleanupMultipart(servletRequest);
            }
        }
    }

    private void handleUpload(@NotNull final MultipartHttpServletRequest request,
                              @NotNull final HttpServletResponse response) throws IOException {
        final RunningBuildEx build = getRunningBuild(request.getHeader("x-nuget-apikey"));
        if (build == null) {
            LOG.debug(INVALID_TOKEN_VALUE);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, INVALID_TOKEN_VALUE);
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
            final BuildLog buildLog = build.getBuildLog();
            final String message = String.format(
                    "NuGet package size is %s bytes which exceeds maximum build artifact file size of %s bytes.\n" +
                            "Consider increasing this limit on the Administration -> Global Settings page.",
                    fileSize, maxArtifactFileSize);
            buildLog.message(message, Status.ERROR, MessageAttrs.serverMessage());
            LOG.debug(message);
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "NuGet package is too large");
            return;
        }

        final Long totalSizeLimit = artifactsLimit.getArtifactsTotalSizeLimit();
        if (totalSizeLimit != null && totalSizeLimit >= 0 && fileSize > totalSizeLimit) {
            final BuildLog buildLog = build.getBuildLog();
            final String message = String.format(
                    "NuGet package size is %s bytes which exceeds elapsed size of build configuration artifacts of %s bytes.\n" +
                            "Consider increasing this limit in the project or build configuration parameters.",
                    fileSize, totalSizeLimit);
            buildLog.message(message, Status.ERROR, MessageAttrs.serverMessage());
            LOG.debug(message);
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
            myStorage.addBuildEntry(build.getBuildId(), NUGET_PROVIDER_ID, id, metadata, !build.isPersonal());
        } catch (Throwable e) {
            LOG.warnAndDebugDetails(String.format("Failed to update %s provider metadata for build %s. Error: %s",
                    NUGET_PROVIDER_ID, build, e.getMessage()), e);
            throw e;
        }

        myCacheReset.resetCache();
    }
}
