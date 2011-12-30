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

import jetbrains.buildServer.nuget.server.feed.server.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.controllers.LatestBuildsCache;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 16:18
 */
public class PackagesIndexImpl implements PackagesIndex {
  private final MetadataStorage myStorage;
  private final BuildsManager myBuilds;
  private final ProjectManager myProjects;

  public PackagesIndexImpl(@NotNull final MetadataStorage storage,
                           @NotNull final BuildsManager builds,
                           @NotNull final ProjectManager projects) {
    myStorage = storage;
    myBuilds = builds;
    myProjects = projects;
  }

  @NotNull
  public Iterator<BuildMetadataEntry> getEntries() {
    return myStorage.getAllEntries(NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID);
  }

  public void processAllPackages(@NotNull Callback callback) {
    new LatestBuildsIterator(callback).serializePackages(getEntries());
  }

  private class LatestBuildsIterator {
    private final Callback myCallback;
    private final LatestBuildsCache myCache;

    private LatestBuildsIterator(@NotNull final Callback callback) {
      myCallback = callback;
      myCache = new LatestBuildsCache(myProjects);
    }

    private void serializePackage(@NotNull final BuildMetadataEntry entry) {

      final Map<String, String> metadata = entry.getMetadata();
      final String buildTypeId = metadata.get(PackagesIndex.TEAMCITY_BUILD_TYPE_ID);

      //skip older entries.
      if (buildTypeId == null) {
        processOldEntry(entry);
      } else {

        final Boolean isLatestVersion = myCache.isLatest(buildTypeId, entry.getBuildId());
        if (isLatestVersion == null) return;

        myCallback.processPackage(
                entry.getKey(),
                metadata,
                buildTypeId,
                entry.getBuildId(),
                isLatestVersion
        );
      }
    }

    private void processOldEntry(@NotNull final BuildMetadataEntry entry) {

      final SBuild aBuild = myBuilds.findBuildInstanceById(entry.getBuildId());
      if (aBuild == null || !(aBuild instanceof SFinishedBuild)) return;
      final SFinishedBuild build = (SFinishedBuild) aBuild;

      final Boolean isLatestVersion = myCache.isLatest(build.getBuildTypeId(), build.getBuildId());
      if (isLatestVersion == null) return;

      final Map<String, String> metadata = new HashMap<String, String>(entry.getMetadata());
      metadata.put("LastUpdated", ODataDataFormat.formatDate(build.getFinishDate()));

      myCallback.processPackage(
              entry.getKey(),
              metadata,
              build.getBuildTypeId(),
              build.getBuildId(),
              isLatestVersion
      );
    }

    public void serializePackages(@NotNull final Iterator<BuildMetadataEntry> entries) {
      final Set<String> reportedPackages = new HashSet<String>();
      while (entries.hasNext()) {
        final BuildMetadataEntry e = entries.next();
        //remove duplicates
        if (!reportedPackages.add(e.getKey())) continue;
        serializePackage(e);
      }
    }
  }
}
