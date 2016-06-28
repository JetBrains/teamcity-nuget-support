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

package jetbrains.buildServer.nuget.server.exec;

import jetbrains.buildServer.nuget.feedReader.NuGetFeedCredentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Object that describes trigger package reference.
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 18:01
 */
public class SourcePackageReference {
  @Nullable
  private final String mySource;
  @Nullable
  private final NuGetFeedCredentials myCredentials;
  @NotNull
  private final String myPackageId;
  @Nullable
  private final String myVersionSpec;
  private final boolean myIncludePrerelease;

  public SourcePackageReference(@Nullable final String source,
                                @NotNull final String packageId,
                                @Nullable final String versionSpec) {
    this(source, packageId, versionSpec, false);
  }

  public SourcePackageReference(@Nullable final String source,
                                @NotNull final String packageId,
                                @Nullable final String versionSpec,
                                          final boolean includePrerelease) {
    this(source, null, packageId, versionSpec, includePrerelease);
  }

  public SourcePackageReference(@Nullable final String source,
                                @Nullable final NuGetFeedCredentials credentials,
                                @NotNull final String packageId,
                                @Nullable final String versionSpec,
                                          final boolean includePrerelease) {
    mySource = source;
    myCredentials = credentials;
    myPackageId = packageId;
    myVersionSpec = versionSpec;
    myIncludePrerelease = includePrerelease;
  }


  @Nullable
  public String getSource() {
    return mySource;
  }

  @Nullable
  public NuGetFeedCredentials getCredentials() {
    return myCredentials;
  }

  @NotNull
  public String getPackageId() {
    return myPackageId;
  }

  @Nullable
  public String getVersionSpec() {
    return myVersionSpec;
  }

  public boolean isIncludePrerelease() {
    return myIncludePrerelease;
  }

  public SourcePackageInfo toInfo(@NotNull final String version) {
    return new SourcePackageInfo(getSource(), getPackageId(), version);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SourcePackageReference that = (SourcePackageReference) o;

    if (!myPackageId.equals(that.myPackageId)) return false;
    if (!equals(mySource, that.mySource)) return false;
    if (!equals(myCredentials, that.myCredentials)) return false;
    if (myVersionSpec != null ? !myVersionSpec.equals(that.myVersionSpec) : that.myVersionSpec != null) return false;
    if (myIncludePrerelease != that.myIncludePrerelease) return false;

    return true;
  }

  private static <T> boolean equals(@Nullable T o1, @Nullable T o2) {
    return o1 == o2 || (o1 != null && o2 != null && o1.equals(o2));
  }

  @Override
  public int hashCode() {
    int result = mySource != null ? mySource.hashCode() : 0;
    result = 31 * result + myPackageId.hashCode();
    result = 31 * result + (myVersionSpec != null ? myVersionSpec.hashCode() : 0);
    result = 31 * result + (myIncludePrerelease ? 42 : 239);
    return result;
  }

  @Override
  public String toString() {
    return "SourcePackageReference{" +
            "mySource='" + mySource + '\'' +
            ", myUsername='" + (myCredentials == null ? "<null>" : myCredentials.getUsername()) + '\'' +
            ", myPackageId='" + myPackageId + '\'' +
            ", myVersionSpec='" + myVersionSpec + '\'' +
            '}';
  }
}
