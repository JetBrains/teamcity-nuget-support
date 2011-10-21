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

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.metadata.ArtifactsMetadataEntry;
import jetbrains.buildServer.util.Dates;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static jetbrains.buildServer.nuget.server.feed.server.PackagesIndex.TEAMCITY_ARTIFACT_RELPATH;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 16:58
 */
public class PackageWriterImpl implements PackagesWriter {
  @NotNull
  private final BuildsManager myBuilds;

  public PackageWriterImpl(@NotNull final BuildsManager builds) {
    myBuilds = builds;
  }

  public void serializePackage(@NotNull ArtifactsMetadataEntry entry, @NotNull Writer writer) throws IOException {
    final SBuild aBuild = myBuilds.findBuildInstanceById(entry.getBuildId());
    if (aBuild == null || !(aBuild instanceof SFinishedBuild)) return;
    final SFinishedBuild build = (SFinishedBuild) aBuild;

    final SBuildType buildType = build.getBuildType();
    final SFinishedBuild latestBuild = buildType == null ? null : buildType.getLastChangesFinished();
    final boolean isLatestVersion = latestBuild == null || latestBuild.getBuildId() == build.getBuildId();

    //The list is generated from
    //JetBrains.TeamCity.NuGet.Feed.Tests.DumpRequiredPackageParameters()
    Map<String, String> parameters = new TreeMap<String, String>(COMPARER);
    parameters.putAll(entry.getMetadata());

    final String relPath = parameters.get(TEAMCITY_ARTIFACT_RELPATH);
    parameters.put("TeamCityDownloadUrl", "/repository/download/" + build.getBuildTypeId() + "/" + entry.getBuildId() + ":id/" + relPath);
    //TBD: parameters.put("ReleaseNotes", "");
    //TBD: parameters.put("Copyright", "");
    parameters.put("IsLatestVersion", String.valueOf(isLatestVersion));
    parameters.put("LastUpdated", formatDate(build.getFinishDate()));

    //extra:
    parameters.put("TeamCityBuildId", String.valueOf(entry.getBuildId()));

    ///it should return same set of parameters as in JetBrains.TeamCity.NuGet.Feed.Repo.TeamCityPackage .NET side class
    writer.write(ServiceMessage.asString("package", parameters));
  }

  @NotNull
  private String formatDate(@NotNull Date date) {
    //TODO:fix timezon printing
    return Dates.formatDate(date, "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("GMT"));
  }

  private static final Comparator<String> COMPARER = new Comparator<String>() {
    private int power(@NotNull String key) {
      if ("Id".equals(key)) return 5;
      if ("Version".equals(key)) return 4;
      if (key.startsWith("teamcity")) return 3;
      return 0;
    }

    public int compare(@NotNull String o1, @NotNull String o2) {
      final int p1 = power(o1);
      final int p2 = power(o2);
      if (p1 > p2) return -1;
      if (p1 < p2) return 1;
      return o1.compareTo(o2);
    }
  };
}
