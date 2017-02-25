/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RealRootDirectory implements DirectoryEntry {
  @NotNull
  public String getName() {
    return "";
  }

  public DirectoryEntry getParent() {
    return null;
  }

  @NotNull
  public DirectoryEntry[] getSubdirectories() {
    ArrayList<DirectoryEntry> result = new ArrayList<DirectoryEntry>();
    if (SystemInfo.isWindows) {
      for (File drive : File.listRoots()) {
        result.add(new RealDirectoryEntry(new FileSystemPath(drive)));
      }
    } else {
      for (File ch : new File("/").listFiles(DIRECTORY_FILTER)) {
        result.add(new RealDirectoryEntry(new FileSystemPath(ch)));
      }
    }

    return result.toArray(new DirectoryEntry[result.size()]);
  }

  @NotNull
  public DirectoryEntry[] getSubdirectories(Collection<String> names) {
    List<DirectoryEntry> entries = new ArrayList<DirectoryEntry>(names.size());
    for (String name : names) {
      entries.add(new RealDirectoryEntry(new FileSystemPath(new File(SystemInfo.isWindows ? name : "/" + name))));
    }
    return entries.toArray(new DirectoryEntry[entries.size()]);
  }

  @NotNull
  public FileEntry[] getFiles() {
    return new FileEntry[0];
  }

  @NotNull
  public FileEntry[] getFiles(@NotNull Collection<String> names) {
    return new FileEntry[0];
  }

  @Override
  public String toString() {
    return "{d:FS_META_ROOT}";
  }

  private final FileFilter DIRECTORY_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  };

}
