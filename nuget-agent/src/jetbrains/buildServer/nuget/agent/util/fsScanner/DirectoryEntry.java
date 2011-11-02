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

package jetbrains.buildServer.nuget.agent.util.fsScanner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface DirectoryEntry {
  @NotNull
  String getName();

  @Nullable
  DirectoryEntry getParent();

  @NotNull
  DirectoryEntry[] getSubdirectories();

  /**
   * @param names names filter
   * @return redured list of items filtered (if possible) by given names
   */
  @NotNull
  DirectoryEntry[] getSubdirectories(Collection<String> names);

  @NotNull
  FileEntry[] getFiles();

  /**
   * @param names names filter
   * @return returns list of existing file names, i.e. subset of given names
   */
  @NotNull
  FileEntry[] getFiles(@NotNull Collection<String> names);
}
