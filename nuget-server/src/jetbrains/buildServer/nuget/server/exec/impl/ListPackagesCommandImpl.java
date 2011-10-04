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

package jetbrains.buildServer.nuget.server.exec.impl;

import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutor;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:10
 */
public class ListPackagesCommandImpl implements ListPackagesCommand {
  private NuGetExecutor myExec;

  public ListPackagesCommandImpl(NuGetExecutor exec) {
    myExec = exec;
  }

  @NotNull
  public Collection<SourcePackageInfo> checkForChanges(@NotNull File nugetPath, @NotNull SourcePackageReference ref) {
    List<String> cmd = new ArrayList<String>();

    cmd.add("TeamCity.List");
    if (!StringUtil.isEmptyOrSpaces(ref.getSource())) {
      cmd.add("-Source");
      cmd.add(ref.getSource());
    }
    cmd.add("-Id");
    cmd.add(ref.getPackageId());

    if (!StringUtil.isEmptyOrSpaces(ref.getVersionSpec())) {
      cmd.add("-Version");
      cmd.add(ref.getVersionSpec());
    }

    return myExec.executeNuGet(nugetPath, cmd, new ListPackageCommandProcessor(ref.getSource()));
  }


  public Map<SourcePackageReference, Collection<SourcePackageInfo>> checkForChanges(@NotNull File nugetPath, @NotNull Collection<SourcePackageReference> refs) {
    return Collections.emptyMap();
  }
}
