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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.cache.ResponseCacheReset;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.serverSide.impl.LogUtil;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataProvider;
import jetbrains.buildServer.serverSide.metadata.MetadataStorageWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex.TEAMCITY_ARTIFACT_RELPATH;
import static jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex.TEAMCITY_BUILD_TYPE_ID;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 12:21
 */
public class NuGetArtifactsMetadataProvider implements BuildMetadataProvider {

  public static final String NUGET_PROVIDER_ID = "nuget";

  private static final Logger LOG = Logger.getInstance(NuGetArtifactsMetadataProvider.class.getName());
  private static final String TEAMCITY_NUGET_INDEX_PACKAGES_PROP_NAME = "teamcity.nuget.index.packages";

  private final LocalNuGetPackageItemsFactory myFactory;
  private final ResponseCacheReset myReset;

  public NuGetArtifactsMetadataProvider(@NotNull final LocalNuGetPackageItemsFactory factory,
                                        @NotNull final ResponseCacheReset reset) {
    myFactory = factory;
    myReset = reset;
  }

  @NotNull
  public String getProviderId() {
    return NUGET_PROVIDER_ID;
  }

  public void generateMedatadata(@NotNull SBuild build, @NotNull MetadataStorageWriter store) {
    if (!TeamCityProperties.getBooleanOrTrue(TEAMCITY_NUGET_INDEX_PACKAGES_PROP_NAME)){
      LOG.info(String.format("Skip NuGet metadata generation for build %s. NuGet packages indexing disabled on the server.", LogUtil.describe(build)));
      return;
    }
    if(!isIndexingEnabledForBuildType(build.getBuildType())){
      LOG.info(String.format("Skip NuGet metadata generation for build %s. NuGet packages indexing disabled for build type %s.", LogUtil.describe(build), LogUtil.describe(build.getBuildType())));
      return;
    }

    LOG.debug("Looking for NuGet packages in " + LogUtil.describe(build));

    final List<BuildArtifact> packages = new ArrayList<BuildArtifact>();
    visitArtifacts(build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getRootArtifact(), packages);

    for (BuildArtifact aPackage : packages) {
      LOG.info("Indexing NuGet package from artifact " + aPackage.getRelativePath() + " of build " + LogUtil.describe(build));
      try {
        Date finishDate = build.getFinishDate();
        if (finishDate == null) finishDate = new Date();

        final Map<String,String> ma = myFactory.loadPackage(aPackage, finishDate);
        ma.put(TEAMCITY_ARTIFACT_RELPATH, aPackage.getRelativePath());
        ma.put(TEAMCITY_BUILD_TYPE_ID, build.getBuildTypeId());

        myReset.resetCache();
        store.addParameters(aPackage.getName(), ma);
      } catch (PackageLoadException e) {
        LOG.warn("Failed to read NuGet package: " + aPackage);
      }
    }
  }

  private boolean isIndexingEnabledForBuildType(@Nullable SBuildType buildType) {
    if(buildType == null) return true;
    final String indexEnabledConfigParamValue = buildType.getConfigParameters().get(TEAMCITY_NUGET_INDEX_PACKAGES_PROP_NAME);
    if(indexEnabledConfigParamValue == null) return true;
    return Boolean.valueOf(indexEnabledConfigParamValue);
  }

  private void visitArtifacts(@NotNull final BuildArtifact artifact, @NotNull final List<BuildArtifact> packages) {
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
}
