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
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.metadata.ArtifactsMetadataEntry;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 16:58
 */
public class PackageWriterImpl implements PackagesWriter {
  @NotNull private final BuildsManager myBuilds;
  @NotNull private final PackagesIndex myIndex;
  @NotNull private final PackageInfoSerializer mySerializer;

  public PackageWriterImpl(@NotNull final BuildsManager builds,
                           @NotNull final PackagesIndex index,
                           @NotNull final PackageInfoSerializer serializer) {
    myBuilds = builds;
    myIndex = index;
    mySerializer = serializer;
  }

  private  void serializePackage(@NotNull final ArtifactsMetadataEntry entry,
                                 @NotNull final Writer writer) throws IOException {

    final SBuild aBuild = myBuilds.findBuildInstanceById(entry.getBuildId());
    if (aBuild == null || !(aBuild instanceof SFinishedBuild)) return;
    final SFinishedBuild build = (SFinishedBuild) aBuild;

    final SBuildType buildType = build.getBuildType();
    final SFinishedBuild latestBuild = buildType == null ? null : buildType.getLastChangesFinished();
    final boolean isLatestVersion = latestBuild == null || latestBuild.getBuildId() == build.getBuildId();

    mySerializer.serializePackage(
            entry.getMetadata(),
            build,
            isLatestVersion,
            writer
    );
  }

  public void serializePackages(@NotNull final HttpServletRequest request,
                                @NotNull final HttpServletResponse response) throws IOException {
    final PrintWriter writer = response.getWriter();

    final Set<String> reportedPackages = new HashSet<String>();

    final Iterator<ArtifactsMetadataEntry> entries = myIndex.getEntries();

    while (entries.hasNext()) {
      final ArtifactsMetadataEntry e = entries.next();
      //remove duplicates
      if (!reportedPackages.add(e.getKey())) continue;

      serializePackage(e, writer);

      writer.write("\r\n");
    }
    writer.flush();
  }
}
