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

package jetbrains.buildServer.nuget.server.exec.impl;

import jetbrains.buildServer.TempFolderProvider;
import jetbrains.buildServer.nuget.server.exec.*;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:10
 */
public class ListPackagesCommandImpl implements ListPackagesCommand {
  private final NuGetExecutor myExec;
  private final TempFolderProvider myTempFiles;

  public ListPackagesCommandImpl(@NotNull final NuGetExecutor exec,
                                 @NotNull final TempFolderProvider tempFiles) {
    myExec = exec;
    myTempFiles = tempFiles;
  }

  @NotNull
  public Map<SourcePackageReference, Collection<SourcePackageInfo>> checkForChanges(@NotNull File nugetPath,
                                                                                    @NotNull Collection<SourcePackageReference> refs) throws NuGetExecutionException {
    final File spec = createTempFile("trigger.spec");
    final File result = createTempFile("trigget.result");

    final ListPackagesArguments argz = new ListPackagesArguments();
    try {
      argz.encodeParameters(spec, refs);
    } catch (IOException e) {
      throw new NuGetExecutionException("Failed to encode parameters. " + e.getMessage(), e);
    }

    try {
      final List<String> cmd = new ArrayList<String>();
      final String commandName = "TeamCity.ListPackages";
      cmd.add(commandName);
      cmd.add("-Request");
      cmd.add(FileUtil.getCanonicalFile(spec).getPath());
      cmd.add("-Response");
      cmd.add(FileUtil.getCanonicalFile(result).getPath());


      return myExec.executeNuGet(nugetPath, cmd, new NuGetOutputProcessorAdapter<Map<SourcePackageReference,Collection<SourcePackageInfo>>>(commandName) {
        @NotNull
        public Map<SourcePackageReference, Collection<SourcePackageInfo>> getResult() throws NuGetExecutionException {
          try {
            return argz.decodeParameters(result);
          } catch (IOException e) {
            throw new NuGetExecutionException("Failed to decode parameters. " + e.getMessage(), e);
          }
        }
      });
    } finally {
      FileUtil.delete(spec);
      FileUtil.delete(result);
    }
  }

  private File createTempFile(@NotNull String infix) throws NuGetExecutionException {
    final File tempDirectory = myTempFiles.getTempDirectory();
    try {
      tempDirectory.mkdirs();
      return FileUtil.createTempFile(tempDirectory, "nuget", infix + ".xml", true);
    } catch (IOException e) {
      throw new NuGetExecutionException("Failed to create temp file at " + tempDirectory + ". " + e.getMessage(), e);
    }
  }
}
