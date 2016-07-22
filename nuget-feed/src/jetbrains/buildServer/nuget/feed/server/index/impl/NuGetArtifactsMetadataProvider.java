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

package jetbrains.buildServer.nuget.feed.server.index.impl;

import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.javaFeed.cache.ResponseCacheReset;
import jetbrains.buildServer.nuget.server.version.FrameworkConstraints;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.serverSide.impl.LogUtil;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataProvider;
import jetbrains.buildServer.serverSide.metadata.MetadataStorageWriter;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;
import static jetbrains.buildServer.nuget.feed.server.index.PackagesIndex.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 12:21
 */
public class NuGetArtifactsMetadataProvider implements BuildMetadataProvider {

  private static final Logger LOG = Logger.getInstance(NuGetArtifactsMetadataProvider.class.getName());

  public static final String NUGET_PROVIDER_ID = "nuget";
  private static final String TEAMCITY_NUGET_INDEX_PACKAGES_PROP_NAME = "teamcity.nuget.index.packages";
  private static final String SHA512 = "SHA512";

  private final ResponseCacheReset myReset;
  @NotNull
  private final NuGetServerSettings myFeedSettings;

  public NuGetArtifactsMetadataProvider(@NotNull final ResponseCacheReset reset, @NotNull final NuGetServerSettings feedSettings) {
    myReset = reset;
    myFeedSettings = feedSettings;
  }

  @NotNull
  public String getProviderId() {
    return NUGET_PROVIDER_ID;
  }

  public void generateMedatadata(@NotNull SBuild build, @NotNull MetadataStorageWriter store) {
    if(!myFeedSettings.isNuGetServerEnabled()){
      LOG.debug(String.format("Skip NuGet metadata generation for build %s. NuGet feed disabled.", LogUtil.describe(build)));
      return;
    }
    if (!TeamCityProperties.getBooleanOrTrue(TEAMCITY_NUGET_INDEX_PACKAGES_PROP_NAME)){
      LOG.info(String.format("Skip NuGet metadata generation for build %s. NuGet packages indexing disabled on the server.", LogUtil.describe(build)));
      return;
    }
    if(!isIndexingEnabledForBuildType(build.getBuildType())){
      LOG.info(String.format("Skip NuGet metadata generation for build %s. NuGet packages indexing disabled for build type %s.", LogUtil.describe(build), LogUtil.describe(build.getBuildType())));
      return;
    }

    LOG.debug("Looking for NuGet packages in " + LogUtil.describe(build));

    final Set<BuildArtifact> packages = new HashSet<>();
    visitArtifacts(build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getRootArtifact(), packages);

    for (final BuildArtifact aPackage : packages) {
      LOG.info("Indexing NuGet package from artifact " + aPackage.getRelativePath() + " of build " + LogUtil.describe(build));
      try {
        final Map<String, String> metadata = generateMetadataForPackage(build, aPackage);
        myReset.resetCache();
        final String key = metadata.get(NuGetPackageAttributes.ID);
        if (key != null) {
          store.addParameters(key, metadata);
          LOG.debug("Added entry to NuGet package index with a key " + key);
        } else {
          LOG.warn("Failed to resolve NuGet package Id, package ignored: " + aPackage);
        }
      } catch (PackageLoadException e) {
        LOG.warn("Failed to read NuGet package: " + aPackage);
      }
    }
  }

  private Map<String, String> generateMetadataForPackage(@NotNull SBuild build, @NotNull BuildArtifact aPackage) throws PackageLoadException {
    final LocalNuGetPackageItemsFactory packageItemsFactory = LocalNuGetPackageItemsFactory.createForBuild(build);
    final FrameworkConstraintsCalculator frameworkConstraintsCalculator = new FrameworkConstraintsCalculator();
    final List<NuGetPackageStructureAnalyser> analysers = Lists.newArrayList(frameworkConstraintsCalculator, packageItemsFactory);
    new NuGetPackageStructureVisitor(analysers).visit(aPackage);

    final Map<String,String> metadata = packageItemsFactory.getItems();
    metadata.put(PACKAGE_HASH, sha512(aPackage));
    metadata.put(PACKAGE_HASH_ALGORITHM, SHA512);
    metadata.put(PACKAGE_SIZE, String.valueOf(aPackage.getSize()));
    metadata.put(TEAMCITY_ARTIFACT_RELPATH, aPackage.getRelativePath());
    metadata.put(TEAMCITY_BUILD_TYPE_ID, build.getBuildTypeId());
    metadata.put(TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(frameworkConstraintsCalculator.getPackageConstraints()));
    return metadata;
  }

  private boolean isIndexingEnabledForBuildType(@Nullable SBuildType buildType) {
    if(buildType == null) return true;
    final String indexEnabledConfigParamValue = buildType.getConfigParameters().get(TEAMCITY_NUGET_INDEX_PACKAGES_PROP_NAME);
    if(indexEnabledConfigParamValue == null) return true;
    return Boolean.valueOf(indexEnabledConfigParamValue);
  }

  private void visitArtifacts(@NotNull final BuildArtifact artifact, @NotNull final Set<BuildArtifact> packages) {
    if (!artifact.isDirectory()) {
      if (FeedConstants.PACKAGE_FILE_NAME_FILTER.accept(artifact.getName())) {
        packages.add(artifact);
      }
      return;
    }

    for (BuildArtifact children : artifact.getChildren()) {
      visitArtifacts(children, packages);
    }
  }

  @NotNull
  private String sha512(@NotNull final BuildArtifact aPackage) throws PackageLoadException {
    InputStream is = null;
    try {
      is = new BufferedInputStream(aPackage.getInputStream());
      final byte[] hash = DigestUtils.sha512(is);
      //Buggy commons.codes added unnecessary newlines
      return Base64.encodeBase64String(hash).replaceAll("[\r\n]+", "");
    } catch (IOException e) {
      throw new PackageLoadException("Failed to compute SHA-512 for " + aPackage);
    } finally {
      FileUtil.close(is);
    }
  }
}
