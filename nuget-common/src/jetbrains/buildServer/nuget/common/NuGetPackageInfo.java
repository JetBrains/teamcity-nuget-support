/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.common;

import jetbrains.buildServer.nuget.common.version.PackageVersion;
import jetbrains.buildServer.nuget.common.version.VersionUtility;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 22:55
 */
public class NuGetPackageInfo implements Comparable<NuGetPackageInfo> {
  private final String myId;
  private final PackageVersion myVersion;

  public NuGetPackageInfo(@NotNull final String id,
                          @NotNull final String version) {
    myId = id;
    myVersion = VersionUtility.valueOf(version);
  }

  public NuGetPackageInfo(@NotNull final String id,
                          @NotNull final PackageVersion version) {
    myId = id;
    myVersion = version;
  }

  public int compareTo(@NotNull final NuGetPackageInfo other) {
    int x;
    if ((x = myId.compareTo(other.myId)) != 0) return x;
    if ((x = myVersion.compareTo(other.myVersion)) != 0) return x;
    return 0;
  }

  @NotNull
  public String getId() {
    return myId;
  }

  @NotNull
  public PackageVersion getVersion() {
    return myVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NuGetPackageInfo that = (NuGetPackageInfo) o;
    return myId.equals(that.myId) && myVersion.equals(that.myVersion);
  }

  @Override
  public int hashCode() {
    int result = myId.hashCode();
    result = 31 * result + myVersion.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "NuGetPackageInfo{" +
            "myId='" + myId + '\'' +
            ", myVersion='" + myVersion + '\'' +
            '}';
  }
}
