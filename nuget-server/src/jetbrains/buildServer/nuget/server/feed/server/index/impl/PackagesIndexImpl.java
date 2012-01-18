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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import jetbrains.buildServer.dataStructures.DecoratingIterator;
import jetbrains.buildServer.dataStructures.Mapper;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 16:18
 */
public class PackagesIndexImpl implements PackagesIndex {
  private final MetadataStorage myStorage;
  private final BuildsManager myBuilds;
  private final ProjectManager myProjects;
  private final SecurityContext myContext;

  public PackagesIndexImpl(@NotNull final MetadataStorage storage,
                           @NotNull final BuildsManager builds,
                           @NotNull final ProjectManager projects,
                           @NotNull final SecurityContext context) {
    myStorage = storage;
    myBuilds = builds;
    myProjects = projects;
    myContext = context;
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
              @Nullable
              private SBuild safeFindBuildInstanceById(long buildId) {
                try {
                  return myBuilds.findBuildInstanceById(buildId);
                } catch (RuntimeException e) {
                  //AccessDeniedException and others could be thrown
                  return null;
                }
              }

              @Nullable
              private String safeFindProjectId(@NotNull final String buildTypeId) {
                try {
                  return myProjects.findProjectId(buildTypeId);
                } catch (RuntimeException e) {
                  return null;
                }
              }

              @Nullable
              private String findBuildTypeId(final long buildId, @NotNull final Map<String, String> metadata) {
                final SBuild aBuild = safeFindBuildInstanceById(buildId);
                if (aBuild == null || !(aBuild instanceof SFinishedBuild)) return null;
                final SFinishedBuild build = (SFinishedBuild) aBuild;

                metadata.put("LastUpdated", ODataDataFormat.formatDate(build.getFinishDate()));
                return build.getBuildTypeId();
              }

              private boolean isAccessible(@NotNull final String buildTypeId) {
                //TODO: move it into BuildMetadataStorage instead.
                //check access to the entry
                final String projectId = safeFindProjectId(buildTypeId);
                //no project no chance
                if (projectId == null) return false;
                //check project access
                return AuthUtil.hasReadAccessTo(myContext.getAuthorityHolder(), projectId);
              }

              @Nullable
              public NuGetIndexEntry mapKey(@NotNull BuildMetadataEntry e) {
                if (!reportedPackages.add(e.getKey())) return null;

                final Map<String, String> metadata = new HashMap<String, String>(e.getMetadata());
                String buildTypeId = metadata.get(PackagesIndex.TEAMCITY_BUILD_TYPE_ID);

                //skip older entries.
                if (buildTypeId == null) {
                  buildTypeId = findBuildTypeId(e.getBuildId(), metadata);
                }

                //still no buildTypeId... skip it than
                if (buildTypeId == null) return null;

                //check access
                if (!isAccessible(buildTypeId)) return null;

                //update isLatestVersion
                final Boolean isLatestVersion = latestCache.isLatest(buildTypeId, e.getBuildId());
                if (isLatestVersion == null) return null;

                metadata.put("TeamCityBuildId", String.valueOf(e.getBuildId()));
                //TODO: consider semVersions here
                metadata.put("IsLatestVersion", String.valueOf(isLatestVersion));
                metadata.put("IsAbsoluteLatestVersion", String.valueOf(isLatestVersion));

                String relPath = metadata.get(TEAMCITY_ARTIFACT_RELPATH);
                if (relPath == null) return null;

                while(relPath.startsWith("/")) relPath = relPath.substring(1);
                final String downloadUrl = "/repository/download/" + buildTypeId + "/" + e.getBuildId() + ":id/" + relPath;
                metadata.put("TeamCityDownloadUrl", downloadUrl);

                return new NuGetIndexEntry(
                        e.getKey(),
                        metadata,
                        buildTypeId,
                        e.getBuildId(),
                        downloadUrl);
              }
            });
  }
}
