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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class RealRootDirectory implements IDirectoryEntry
  {
    @NotNull
    public String Name()
    {
      return "";
    }

    public IDirectoryEntry Parent()
    {
      return null;
    }

    @NotNull
    public IDirectoryEntry[] Subdirectories()
    {
      {
        ArrayList<IDirectoryEntry> result = new ArrayList<IDirectoryEntry>();
        if (SystemInfo.isWindows)
        {          
          for (File drive : File.listRoots())
          {
            result.add(new RealDirectoryEntry(new FileSystemPath(drive)));
          }         
        } else
        {
          for (File ch : new File("/").listFiles(new FileFilter() {
            public boolean accept(File pathname) {
              return pathname.isDirectory();
            }
          }))
          {
            result.add(new RealDirectoryEntry(new FileSystemPath(ch)));
          }
        }

        return result.toArray(new IDirectoryEntry[result.size()]);
      }
    }

    @NotNull
    public IFileEntry[] Files()
    {
      return new IFileEntry[0];
    }

    @Override
    public String toString()
    {
      return "{d:FS_META_ROOT}";
    }
  }
