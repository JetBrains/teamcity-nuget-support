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

import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.07.11 12:49
 */
public interface RepositoryConfigReader {

  /**
   * Read repositories.config file and pushes callbacks for every found reference
   * @param nugetRepsitoryConfig path
   * @param callback callback
   * @throws IOException exception if no file found, or file read or xml parse errors
   */
  void readConfigurations(@NotNull RelativePath nugetRepsitoryConfig,
                          @NotNull Callback callback) throws IOException;

  public interface Callback {
    /**
     * Called when packages.config is found
     *
     * @param configRelativePath relative path to NuGet configuration
     */
    void onPackagesConfigFound(@NotNull final RelativePath configRelativePath);
  }
}
