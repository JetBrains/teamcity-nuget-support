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
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.util.Dates;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static jetbrains.buildServer.nuget.server.feed.server.PackagesIndex.TEAMCITY_ARTIFACT_RELPATH;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 24.10.11 17:53
 */
public class PackageInfoSerializer {
  private final MetadataControllersPaths myPaths;

  public PackageInfoSerializer(@NotNull final MetadataControllersPaths paths) {
    myPaths = paths;
  }

  public void serializePackage(@NotNull final Map<String, String> pacakgeParameters,
                               @NotNull final SFinishedBuild build,
                               final boolean isLatestVersion,
                               @NotNull final Writer writer) throws IOException {

    //The list is generated from
    //JetBrains.TeamCity.NuGet.Feed.Tests.DumpRequiredPackageParameters()
    Map<String, String> parameters = new TreeMap<String, String>(COMPARER);
    parameters.putAll(pacakgeParameters);

    final String relPath = parameters.get(TEAMCITY_ARTIFACT_RELPATH);
    parameters.put("TeamCityDownloadUrl", myPaths.getArtifactDownloadUrl(build.getBuildTypeId(), build.getBuildId(), relPath));
    //TBD: parameters.put("ReleaseNotes", "");
    //TBD: parameters.put("Copyright", "");
    parameters.put("IsLatestVersion", String.valueOf(isLatestVersion));
    parameters.put("LastUpdated", formatDate(build.getFinishDate()));

    //extra:
    parameters.put("TeamCityBuildId", String.valueOf(build.getBuildId()));

    ///it should return same set of parameters as in JetBrains.TeamCity.NuGet.Feed.Repo.TeamCityPackage .NET side class
    writer.write(ServiceMessage.asString("package", parameters));
  }

  @NotNull
  private String formatDate(@NotNull final Date date) {
    //TODO:fix timezone printing
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
