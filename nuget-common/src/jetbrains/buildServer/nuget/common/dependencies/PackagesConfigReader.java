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

package jetbrains.buildServer.nuget.common.dependencies;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Provide parsing for NuGet packages.config file
 *
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.07.11 12:48
 */
public interface PackagesConfigReader {

  void readConfig(@NotNull RelativePath path,
                  @NotNull Callback callback) throws IOException;

  public interface Callback {
    /**
     * Called for each new found package usage in project
     * @param id nuget package Id
     * @param version nuget package version
     * @param allowedVersions nuget allowed version info, if specified
     */
    void packageFound(@NotNull String id, @NotNull String version, @Nullable String allowedVersions);
  }
}
