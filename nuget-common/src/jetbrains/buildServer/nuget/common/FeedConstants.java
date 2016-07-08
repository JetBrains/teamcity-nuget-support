/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.common;

import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 16:04
 */
public class FeedConstants {
  public static final String NUGET_FEED_V1 = "http://packages.nuget.org/api/v1/FeedService.svc";
  public static final String NUGET_FEED_V2 = "http://packages.nuget.org/api/v2";

  public static final String PATH_TO_NUGET_EXE = "tools/NuGet.exe";
  public static final String NUGET_COMMANDLINE = "NuGet.CommandLine";
  public static final String NUGET_EXTENSION = ".nupkg";
  public static final String EXE_EXTENSION = ".exe";
  public static final String NUGET_SUPPORTED_PROJECTS[] = {".csproj", ".vbproj", ".fsproj", ".nproj"}; //see PackCommand.cs
  public static final String NUGET_SYMBOLS_EXTENSION = ".symbols.nupkg";
  public static final String NUSPEC_FILE_EXTENSION = ".nuspec";

  public static final FileFilter PACKAGE_FILE_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      return pathname.isFile() && PACKAGE_FILE_NAME_FILTER.accept(pathname.getName());
    }
  };

  public static final FileFilter EXE_FILE_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      return pathname.isFile() && EXE_FILE_NAME_FILTER.accept(pathname.getName());
    }
  };

  public static final FileFilter NUGET_TOOL_FILE_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      final String name = pathname.getName();
      return pathname.isFile() && name.startsWith(NUGET_COMMANDLINE) && (PACKAGE_FILE_NAME_FILTER.accept(name) || EXE_FILE_NAME_FILTER.accept(name));
    }
  };

  public static final Filter<String> EXE_FILE_NAME_FILTER = new Filter<String>() {
    public boolean accept(@NotNull String data) {
      return data.toLowerCase().endsWith(FeedConstants.EXE_EXTENSION.toLowerCase());
    }
  };

  public static final Filter<String> PACKAGE_FILE_NAME_FILTER = new Filter<String>() {
    public boolean accept(@NotNull String data) {
      data = data.toLowerCase();
      return data.endsWith(FeedConstants.NUGET_EXTENSION.toLowerCase()) && !data.endsWith(FeedConstants.NUGET_SYMBOLS_EXTENSION.toLowerCase());
    }
  };
}
