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

package jetbrains.buildServer.nuget.common;

import jetbrains.buildServer.nuget.feedReader.NuGetPackageInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 26.06.12 14:50
 */
public class SourcePackageInfo implements Comparable<SourcePackageInfo> {
  private final NuGetPackageInfo myPackageInfo;
  private final String mySource;

  public SourcePackageInfo(@NotNull final NuGetPackageInfo packageInfo,
                           @Nullable final String source) {
    myPackageInfo = packageInfo;
    mySource = source;
  }

  @NotNull
  public NuGetPackageInfo getPackageInfo() {
    return myPackageInfo;
  }

  @Nullable
  public String getSource() {
    return mySource;
  }

  public int compareTo(@NotNull final SourcePackageInfo o) {
    int x;
    if ((x = this.myPackageInfo.compareTo(o.myPackageInfo)) != 0) return x;
    if (this.mySource == null) return o.mySource == null ? 0 : 1;
    if (o.mySource == null) return -1;
    return this.mySource.compareToIgnoreCase(o.mySource);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SourcePackageInfo)) return false;

    SourcePackageInfo that = (SourcePackageInfo) o;

    if (!myPackageInfo.equals(that.myPackageInfo)) return false;
    if (mySource != null ? !mySource.equals(that.mySource) : that.mySource != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myPackageInfo.hashCode();
    result = 31 * result + (mySource != null ? mySource.hashCode() : 0);
    return result;
  }
}
