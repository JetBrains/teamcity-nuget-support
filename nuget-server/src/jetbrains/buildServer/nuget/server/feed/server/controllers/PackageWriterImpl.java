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

package jetbrains.buildServer.nuget.server.feed.server.controllers;

import jetbrains.buildServer.nuget.server.feed.server.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.index.ODataDataFormat;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 16:58
 */
public class PackageWriterImpl implements PackagesWriter {
  @NotNull
  private final PackagesIndex myIndex;
  @NotNull
  private final PackageInfoSerializer mySerializer;
  @NotNull
  private final BuildsManager myBuilds;
  @NotNull
  private final ProjectManager myProjectManager;

  public PackageWriterImpl(@NotNull final PackagesIndex index,
                           @NotNull final BuildsManager builds,
                           @NotNull final PackageInfoSerializer serializer,
                           @NotNull final ProjectManager projectManager) {
    myBuilds = builds;
    myProjectManager = projectManager;
    myIndex = index;
    mySerializer = serializer;
  }

  private class LatestBuildsCache {
    private final Map<String, Long> myBuildTypeToLatest = new HashMap<String, Long>();

    @Nullable
    public Boolean isLatest(@NotNull final String buildTypeId, final long buildId) {
      Long build = myBuildTypeToLatest.get(buildTypeId);
      if (build == null) {
        final SBuildType buildTypeById = myProjectManager.findBuildTypeById(buildTypeId);
        //skip project if no build type found
        if (buildTypeById == null) return null;

        final SFinishedBuild lastChangesFinished = buildTypeById.getLastChangesFinished();
        //no latest build found, than skip this build
        if (lastChangesFinished == null) return null;

        myBuildTypeToLatest.put(buildTypeId, build = lastChangesFinished.getBuildId());
      }
      return build == buildId;
    }
  }

  private void serializePackage(@NotNull final LatestBuildsCache cache,
                                @NotNull final BuildMetadataEntry entry,
                                @NotNull final Writer writer) throws IOException {

    final Map<String, String> metadata = entry.getMetadata();
    final String buildTypeId = metadata.get(PackagesIndex.TEAMCITY_BUILD_TYPE_ID);

    //skip older entries.
    if (buildTypeId == null) {
      processOldEntry(cache, entry, writer);
    } else {

      final Boolean isLatestVersion = cache.isLatest(buildTypeId, entry.getBuildId());
      if (isLatestVersion == null) return;

      mySerializer.serializePackage(
              metadata,
              buildTypeId,
              entry.getBuildId(),
              isLatestVersion,
              writer
      );
    }
  }

  private void processOldEntry(@NotNull final LatestBuildsCache cache,
                               @NotNull final BuildMetadataEntry entry,
                               @NotNull final Writer writer) throws IOException {

    final SBuild aBuild = myBuilds.findBuildInstanceById(entry.getBuildId());
    if (aBuild == null || !(aBuild instanceof SFinishedBuild)) return;
    final SFinishedBuild build = (SFinishedBuild) aBuild;

    final Boolean isLatestVersion = cache.isLatest(build.getBuildTypeId(), build.getBuildId());
    if (isLatestVersion == null) return;

    final Map<String, String> metadata = new HashMap<String, String>(entry.getMetadata());
    metadata.put("LastUpdated", ODataDataFormat.formatDate(build.getFinishDate()));

    mySerializer.serializePackage(
                metadata,
                build.getBuildTypeId(),
                build.getBuildId(),
                isLatestVersion,
                writer
        );
  }

  public void serializePackages(@NotNull final HttpServletRequest request,
                                @NotNull final HttpServletResponse response) throws IOException {
    final PrintWriter writer = response.getWriter();
    final Set<String> reportedPackages = new HashSet<String>();
    final Iterator<BuildMetadataEntry> entries = myIndex.getEntries();

    final LatestBuildsCache cache = new LatestBuildsCache();

    while (entries.hasNext()) {
      final BuildMetadataEntry e = entries.next();
      //remove duplicates
      if (!reportedPackages.add(e.getKey())) continue;
      serializePackage(cache, e, writer);
      writer.write("\r\n");
    }
    writer.flush();
  }
}