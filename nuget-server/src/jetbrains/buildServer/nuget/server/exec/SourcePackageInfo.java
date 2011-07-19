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

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:17
 */
public class SourcePackageInfo {
  private final String mySource;
  private final String myPackageId;
  private final String myVersion;

  public SourcePackageInfo(@NotNull final String source,
                           @NotNull final String packageId,
                           @NotNull final String version) {
    mySource = source;
    myPackageId = packageId;
    myVersion = version;
  }

  @NotNull
  public String getSource() {
    return mySource;
  }

  @NotNull
  public String getPackageId() {
    return myPackageId;
  }

  @NotNull
  public String getVersion() {
    return myVersion;
  }

  @Override
  public String toString() {
    return "SourcePackageInfo{" +
            "mySource='" + mySource + '\'' +
            ", myPackageId='" + myPackageId + '\'' +
            ", myVersion='" + myVersion + '\'' +
            '}';
  }
}
