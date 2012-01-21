/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.index.impl.transform;

import jetbrains.buildServer.nuget.server.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackageTransformation;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.01.12 20:31
*/
public class IsLatestFieldTransformation implements PackageTransformation {
  private final Set<String> myReleasedPackages = new HashSet<String>();
  private final Set<String> myAllPackages = new HashSet<String>();

  @NotNull
  public Status applyTransformation(@NotNull final NuGetPackageBuilder builder) {
    final String packageName = builder.getPackageName();
    final String version = builder.getVersion();

    //release or preselease version is parsed from package information according for semver.org
    //http://semver.org/
    //http://docs.nuget.org/docs/reference/versioning
    final boolean isReleaseVersion = version.matches("^\\d+(\\.\\d+)+$");

    //Metadata entries are sorted from newer to older packages
    //isLatestVersion === this is the firts occurence of package in the collection
    final boolean isLatestVersion = isReleaseVersion && myReleasedPackages.add(packageName);
    final boolean isAbsoluteLatestVersion = myAllPackages.add(packageName);

    //here we assume there is a package with full version
    //otherwise there will be a feed with packages without specified IsLatestVersion == true package

    //Note, here we assume the package version is always incremented by the time,
    //Note, thus there is no need to take case about comparison of version
    //Note, i.e. 1.0.0+build ? 1.1.0-release ? 1.0.0 ? 1.1.1 and so on.
    builder.setMetadata("IsLatestVersion", String.valueOf(isLatestVersion));
    builder.setMetadata("IsAbsoluteLatestVersion", String.valueOf(isAbsoluteLatestVersion));
    return Status.CONTINUE;
  }
}
