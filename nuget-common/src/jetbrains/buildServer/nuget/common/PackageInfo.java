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

package jetbrains.buildServer.nuget.common;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 22:55
 */
public class PackageInfo implements Comparable<PackageInfo> {
  private final String myId;
  private final String myVersion;

  public PackageInfo(@NotNull final String id,
                     @NotNull final String version) {
    myId = id;
    myVersion = version;
  }

  public int compareTo(PackageInfo o) {
    int x;
    if ((x = getId().compareTo(o.getId())) != 0) return x;
    if ((x = getVersion().compareTo(o.getVersion())) != 0) return x;
    return 0;
  }

  @NotNull
  public String getId() {
    return myId;
  }

  @NotNull
  public String getVersion() {
    return myVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PackageInfo that = (PackageInfo) o;
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
    return "PackageInfo{" +
            "myId='" + myId + '\'' +
            ", myVersion='" + myVersion + '\'' +
            '}';
  }
}
