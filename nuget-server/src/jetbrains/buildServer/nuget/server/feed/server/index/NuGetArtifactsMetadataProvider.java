/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.index;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.serverSide.impl.LogUtil;
import jetbrains.buildServer.serverSide.metadata.ArtifactsMetadataProvider;
import jetbrains.buildServer.serverSide.metadata.ArtifactsMetadataStorageWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.nuget.server.feed.server.PackagesIndex.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 12:21
 */
public class NuGetArtifactsMetadataProvider implements ArtifactsMetadataProvider {
  private static final Logger LOG = Logger.getInstance(NuGetArtifactsMetadataProvider.class.getName());
  public static final String NUGET_PROVIDER_ID = "nuget";

  private final LocalNuGetPackageItemsFactory myFactory;

  public NuGetArtifactsMetadataProvider(@NotNull final LocalNuGetPackageItemsFactory myFactory) {
    this.myFactory = myFactory;
  }

  @NotNull
  public String getMetadataProviderId() {
    return NUGET_PROVIDER_ID;
  }

  private void visitArtifacts(@NotNull final BuildArtifact artifact, @NotNull final List<BuildArtifact> packages) {
    if (!artifact.isDirectory()) {
      if (artifact.getName().endsWith(".nupkg")) {
        packages.add(artifact);
      }
      return;
    }

    for (BuildArtifact children : artifact.getChildren()) {
      visitArtifacts(children, packages);
    }
  }

  public void generateMedatadata(@NotNull SBuild build, @NotNull ArtifactsMetadataStorageWriter store) {
    LOG.debug("Looking for NuGet packages in " + LogUtil.describe(build));

    final List<BuildArtifact> packages = new ArrayList<BuildArtifact>();
    visitArtifacts(build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).getRootArtifact(), packages);

    if (packages.isEmpty()) return;

    for (BuildArtifact aPackage : packages) {
      try {
        final Map<String,String> ma = myFactory.loadPackage(aPackage);

        ma.put(TEAMCITY_ARTIFACT_RELPATH, aPackage.getRelativePath());
        ma.put(TEAMCITY_BUILD_TYPE_ID, build.getBuildTypeId());
        ma.put(TEAMCITY_BUILD_TYPE_NAME, build.getBuildTypeName());
        ma.put(TEAMCITY_BUILD_ID, String.valueOf(build.getBuildId()));

        store.addParameters(aPackage.getName(), ma);
      } catch (PackageLoadException e) {
        LOG.warn("Failed to read nuget package: " + aPackage);
      }
    }
  }
}
