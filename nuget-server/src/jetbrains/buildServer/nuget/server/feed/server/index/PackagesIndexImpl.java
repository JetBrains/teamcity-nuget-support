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

import jetbrains.buildServer.dataStructures.DecoratingIterator;
import jetbrains.buildServer.dataStructures.Mapper;
import jetbrains.buildServer.nuget.server.feed.server.NuGetIndexEntry;
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

  @NotNull
  public Iterator<NuGetIndexEntry> getNuGetEntries() {
    final Set<String> reportedPackages = new HashSet<String>();
    final LatestBuildsCache latestCache = new LatestBuildsCache(myProjects);

    return new DecoratingIterator<NuGetIndexEntry, BuildMetadataEntry>(
            getEntries(),
            new Mapper<BuildMetadataEntry, NuGetIndexEntry>() {
              public NuGetIndexEntry mapKey(@NotNull BuildMetadataEntry e) {
                if (!reportedPackages.add(e.getKey())) return null;

                final Map<String, String> metadata = new HashMap<String, String>(e.getMetadata());
                final String buildTypeId = metadata.get(PackagesIndex.TEAMCITY_BUILD_TYPE_ID);

                //skip older entries.
                if (buildTypeId == null) {
                  final SBuild aBuild = myBuilds.findBuildInstanceById(e.getBuildId());
                  if (aBuild == null || !(aBuild instanceof SFinishedBuild)) return null;
                  final SFinishedBuild build = (SFinishedBuild) aBuild;

                  final Boolean isLatestVersion = latestCache.isLatest(build.getBuildTypeId(), build.getBuildId());
                  if (isLatestVersion == null) return null;

                  metadata.put("LastUpdated", ODataDataFormat.formatDate(build.getFinishDate()));

                  return new NuGetIndexEntry(
                          e.getKey(),
                          metadata,
                          build.getBuildTypeId(),
                          build.getBuildId(),
                          isLatestVersion
                  );
                }


                final Boolean isLatestVersion = latestCache.isLatest(buildTypeId, e.getBuildId());
                if (isLatestVersion == null) return null;

                return new NuGetIndexEntry(
                        e.getKey(),
                        metadata,
                        buildTypeId,
                        e.getBuildId(),
                        isLatestVersion
                );
              }
            });
  }
}
