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

import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class RealFileSystem implements FileSystem {
  public static final DirectoryEntry ROOT = new RealRootDirectory();

  public boolean isPathAbsolute(@NotNull String path) {
    if (SystemInfo.isWindows) {
      if ((path.startsWith("/") || path.startsWith("\\"))) return false;
      return new File(path).isAbsolute();
    }
    return path.startsWith("/");
  }

  @NotNull
  public DirectoryEntry getRoot() {
    return ROOT;
  }

  public boolean caseSensitive() {
    return SystemInfo.isFileSystemCaseSensitive;
  }
}

