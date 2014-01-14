/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.reader;

import jetbrains.buildServer.nuget.common.PackageInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 13:43
 */
public class FeedPackage implements Comparable<FeedPackage>{
  private final String myAtomId;
  private final PackageInfo myInfo;
  private final boolean myLatestVersion;
  private final String myDescription;

  private final String myDownloadUrl;

  public FeedPackage(@NotNull String atomId,
                     @NotNull PackageInfo info,
                     boolean latestVersion,
                     @NotNull String description,
                     @NotNull String downloadUrl) {
    myAtomId = atomId;
    myInfo = info;
    myLatestVersion = latestVersion;
    myDescription = description;
    myDownloadUrl = downloadUrl;
  }

  @NotNull
  public String getAtomId() {
    return myAtomId;
  }

  @NotNull
  public PackageInfo getInfo() {
    return myInfo;
  }

  public boolean isLatestVersion() {
    return myLatestVersion;
  }

  @NotNull
  public String getDescription() {
    return myDescription;
  }

  @NotNull
  public String getDownloadUrl() {
    return myDownloadUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FeedPackage that = (FeedPackage) o;
    return myInfo.equals(that.myInfo);
  }

  @Override
  public int hashCode() {
    return myInfo.hashCode();
  }

  public int compareTo(@NotNull FeedPackage o) {
    return myInfo.compareTo(o.myInfo);
  }

  @Override
  public String toString() {
    return "FeedPackage{" +
            "myAtomId='" + myAtomId + '\'' +
            ", myInfo=" + myInfo +
            ", myLatestVersion=" + myLatestVersion +
            ", myDescription='" + myDescription + '\'' +
            ", myDownloadUrl='" + myDownloadUrl + '\'' +
            '}';
  }
}
