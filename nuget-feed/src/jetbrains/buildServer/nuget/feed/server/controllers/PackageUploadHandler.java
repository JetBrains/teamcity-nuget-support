package jetbrains.buildServer.nuget.feed.server.controllers;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.index.impl.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageStructureVisitor;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.*;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.ID;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.NORMALIZED_VERSION;

/**
 * NuGet package upload handler.
 */
public class PackageUploadHandler implements NuGetFeedHandler {
    private static final Logger LOG = Logger.getInstance(PackageUploadHandler.class.getName());
    private static final String INVALID_TOKEN_VALUE = "Invalid token value";
    private static final String INVALID_PACKAGE_CONTENTS = "Invalid NuGet package contents";
    private final EventDispatcher<BuildServerListener> myEventDispatcher;
    private final RunningBuildsCollection myRunningBuilds;
    private final ServerSettings myServerSettings;

    public PackageUploadHandler(@NotNull final EventDispatcher<BuildServerListener> eventDispatcher,
                                @NotNull final RunningBuildsCollection runningBuilds,
                                @NotNull final ServerSettings serverSettings) {
        myEventDispatcher = eventDispatcher;
        myRunningBuilds = runningBuilds;
        myServerSettings = serverSettings;
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_TOKEN_VALUE);
            return;
        }

        final String token = EncryptUtil.unscramble(tokenValue);
        if (!token.startsWith(NuGetFeedConstants.BUILD_TOKEN_PREFIX)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_TOKEN_VALUE);
            return;
        }

        long buildId;
        try {
            buildId = Long.parseLong(token.substring(NuGetFeedConstants.BUILD_TOKEN_PREFIX.length()));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_TOKEN_VALUE);
            return;
        }

        final MultipartFile file = request.getFile("package");
        if (file == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "NuGet package data not found");
            return;
        }

        if (file.getSize() > myServerSettings.getMaximumAllowedArtifactSize()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "NuGet package is too large");
            return;
        }

        final RunningBuildEx build = myRunningBuilds.findRunningBuildById(buildId);
        if (build == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_TOKEN_VALUE);
            return;
        }

        final LocalNuGetPackageItemsFactory packageItemsFactory = LocalNuGetPackageItemsFactory.createForBuild(build);
        final NuGetPackageStructureVisitor structureVisitor = new NuGetPackageStructureVisitor(Collections.singletonList(packageItemsFactory));

        try {
            structureVisitor.visit(file.getName(), file.getInputStream());
            final Map<String, String> items = packageItemsFactory.getItems();
            final String id = items.get(ID);
            final String version = items.get(NORMALIZED_VERSION);
            if (StringUtil.isEmptyOrSpaces(id) || StringUtil.isEmptyOrSpaces(version)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid NuGet package contents");
            } else {
                final String path = MessageFormat.format(".nuget/packages/{0}/{1}/{0}.{1}.nupkg", id, version);
                build.publishArtifact(path, file.getInputStream());
                myEventDispatcher.getMulticaster().buildArtifactsChanged(build);
            }
        } catch (PackageLoadException e) {
            LOG.warn("Failed to read package: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, INVALID_PACKAGE_CONTENTS);
        }
    }
}
