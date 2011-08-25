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
package jetbrains.buildServer.nuget.agent.runner.publish.fsScanner;


import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RealDirectoryEntry implements IDirectoryEntry {
  private final FileSystemPath myPath;

  public RealDirectoryEntry(FileSystemPath path) {
    myPath = path;
  }

  @NotNull
  public String Name() {
    {
      String name = myPath.Name();
      if (SystemInfo.isWindows) {
        if (name.length() == 3 && name.charAt(1) == ':')
          return name.charAt(0) + ":";
      }

      return name;
    }
  }

  public IDirectoryEntry Parent() {
    if (StringUtil.isEmptyOrSpaces(myPath.FilePath().getPath()))
      return RealFileSystem.ROOT;

    String parent = myPath.FilePath().getParent();
    if (parent == null)
      return RealFileSystem.ROOT;

    FileSystemPath parentPath = new FileSystemPath(parent);
    if (StringUtil.isEmptyOrSpaces(parentPath.FilePath().getPath()))
      return RealFileSystem.ROOT;

    return new RealDirectoryEntry(parentPath);
  }

  @NotNull
  public IDirectoryEntry[] Subdirectories() {
    try {
      List<IDirectoryEntry> list = new ArrayList<IDirectoryEntry>();
      for (File dir : FilePath().listFiles(new FileFilter() {
        public boolean accept(File pathname) {
          return pathname.isDirectory();
        }
      })) {
        list.add(new RealDirectoryEntry(new FileSystemPath(dir)));
      }
      return list.toArray(new IDirectoryEntry[list.size()]);
    } catch (Exception e) {
      return new IDirectoryEntry[0];
    }
  }

  @NotNull
  public IDirectoryEntry[] Subdirectories(Collection<String> names) {
    List<IDirectoryEntry> entries = new ArrayList<IDirectoryEntry>(names.size());
    for (String name : names) {
      entries.add(new RealDirectoryEntry(new FileSystemPath(new File(FilePath(), name))));
    }
    return entries.toArray(new IDirectoryEntry[entries.size()]);
  }

  private File FilePath() {
    String filePath = myPath.FilePath().getPath();

    if (SystemInfo.isWindows && filePath.endsWith(":")) {
      return new File(filePath + "\\");
    }
    if (!SystemInfo.isWindows && filePath.equals("")) {
      return new File("/");
    }

    return new File(filePath);
  }

  @NotNull
  public IFileEntry[] Files() {

    try {
      List<IFileEntry> list = new ArrayList<IFileEntry>();
      for (File dir : FilePath().listFiles(new FileFilter() {
        public boolean accept(File pathname) {
          return pathname.isFile();
        }
      })) {
        list.add(new RealFileEntry(new FileSystemPath(dir)));
      }
      return list.toArray(new IFileEntry[list.size()]);
    } catch (Exception e) {
      return new IFileEntry[0];
    }
  }

  @NotNull
  public IFileEntry[] Files(Collection<String> names) {
    List<IFileEntry> list = new ArrayList<IFileEntry>();
    for (String name : names) {
      final File file = new File(FilePath(), name);
      if (!file.isFile()) continue;
      list.add(new RealFileEntry(new FileSystemPath(file)));
    }
    return list.toArray(new IFileEntry[list.size()]);
  }

  @Override
  public String toString() {
    return "{d:" + myPath.FilePath() + "|" + Name() + "}";
  }
}
