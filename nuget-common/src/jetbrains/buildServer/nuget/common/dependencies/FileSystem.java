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

import java.io.File;

/**
 * Represents virual file to allow using it both
 * on server (where no checkout directory) and on agent
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.07.11 21:19
 */
public interface FileSystem {
  /**
   * @return root file. Maps to build checkout directory
   */
  @NotNull
  RelativePath getRoot();

  /**
   * @param relativePath relative path
   * @return a file that contains content of a given by RelativePath.
   *         Returned file may be a temporary file
   */
  @Nullable
  File getFile(@NotNull final RelativePath relativePath);
}
