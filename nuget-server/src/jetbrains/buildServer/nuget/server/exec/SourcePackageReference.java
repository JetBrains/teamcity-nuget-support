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

package jetbrains.buildServer.nuget.server.exec;

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
  @NotNull
  private final String myPackageId;
  @Nullable
  private final String myVersionSpec;

  public SourcePackageReference(@Nullable final String source,
                                @NotNull final String packageId,
                                @Nullable final String versionSpec) {
    mySource = source;
    myPackageId = packageId;
    myVersionSpec = versionSpec;
  }

  @Nullable
  public String getSource() {
    return mySource;
  }

  @NotNull
  public String getPackageId() {
    return myPackageId;
  }

  @Nullable
  public String getVersionSpec() {
    return myVersionSpec;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SourcePackageReference that = (SourcePackageReference) o;

    if (!myPackageId.equals(that.myPackageId)) return false;
    if (mySource != null ? !mySource.equals(that.mySource) : that.mySource != null) return false;
    if (myVersionSpec != null ? !myVersionSpec.equals(that.myVersionSpec) : that.myVersionSpec != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = mySource != null ? mySource.hashCode() : 0;
    result = 31 * result + myPackageId.hashCode();
    result = 31 * result + (myVersionSpec != null ? myVersionSpec.hashCode() : 0);
    return result;
  }
}
