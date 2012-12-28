/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RealDirectoryEntry implements DirectoryEntry {
  private final FileSystemPath myPath;

  public RealDirectoryEntry(FileSystemPath path) {
    myPath = path;
  }

  @NotNull
  public String getName() {

    String name = myPath.getName();
    if (SystemInfo.isWindows) {
      if (name.length() == 3 && name.charAt(1) == ':')
        return name.charAt(0) + ":";
    }

    return name;

  }

  public DirectoryEntry getParent() {
    if (StringUtil.isEmptyOrSpaces(myPath.getFilePath().getPath()))
      return RealFileSystem.ROOT;

    String parent = myPath.getFilePath().getParent();
    if (parent == null)
      return RealFileSystem.ROOT;

    FileSystemPath parentPath = new FileSystemPath(parent);
    if (StringUtil.isEmptyOrSpaces(parentPath.getFilePath().getPath()))
      return RealFileSystem.ROOT;

    return new RealDirectoryEntry(parentPath);
  }

  @NotNull
  public DirectoryEntry[] getSubdirectories() {
    try {
      List<DirectoryEntry> list = new ArrayList<DirectoryEntry>();
      for (File dir : FilePath().listFiles(DIRECTORY_FILTER)) {
        list.add(new RealDirectoryEntry(new FileSystemPath(dir)));
      }
      return list.toArray(new DirectoryEntry[list.size()]);
    } catch (Exception e) {
      return new DirectoryEntry[0];
    }
  }

  @NotNull
  public DirectoryEntry[] getSubdirectories(Collection<String> names) {
    List<DirectoryEntry> entries = new ArrayList<DirectoryEntry>(names.size());
    for (String name : names) {
      entries.add(new RealDirectoryEntry(new FileSystemPath(new File(FilePath(), name))));
    }
    return entries.toArray(new DirectoryEntry[entries.size()]);
  }

  private File FilePath() {
    String filePath = myPath.getFilePath().getPath();

    if (SystemInfo.isWindows && filePath.endsWith(":")) {
      return new File(filePath + "\\");
    }
    if (!SystemInfo.isWindows && filePath.equals("")) {
      return new File("/");
    }

    return new File(filePath);
  }

  @NotNull
  public FileEntry[] getFiles() {

    try {
      List<FileEntry> list = new ArrayList<FileEntry>();
      for (File dir : FilePath().listFiles(FILE_FILTER)) {
        list.add(new RealFileEntry(new FileSystemPath(dir)));
      }
      return list.toArray(new FileEntry[list.size()]);
    } catch (Exception e) {
      return new FileEntry[0];
    }
  }

  @NotNull
  public FileEntry[] getFiles(@NotNull Collection<String> names) {
    List<FileEntry> list = new ArrayList<FileEntry>();
    for (String name : names) {
      final File file = new File(FilePath(), name);
      if (!file.isFile()) continue;
      list.add(new RealFileEntry(new FileSystemPath(file)));
    }
    return list.toArray(new FileEntry[list.size()]);
  }

  @Override
  public String toString() {
    return "{d:" + myPath.getFilePath() + "|" + getName() + "}";
  }

  private final FileFilter DIRECTORY_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  };

  private final FileFilter FILE_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      return pathname.isFile();
    }
  };

}
