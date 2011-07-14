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

package jetbrains.buildServer.nuget.server.exec;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:10
 */
public class ListPackagesCommand {
  private static final Logger LOG = Logger.getInstance(ListPackagesCommand.class.getName());

  private NuGetExecutor myExec;

  public ListPackagesCommand(NuGetExecutor exec) {
    myExec = exec;
  }

  public Collection<PackageInfo> checkForChanges(
          @NotNull final String source,
          @NotNull final String packageId,
          @Nullable final String versionSpec) {
    List<String> cmd = new ArrayList<String>();

    cmd.add("TeamCity.List");
    cmd.add("-Source");
    cmd.add(source);
    cmd.add("-Id");
    cmd.add(packageId);

    if (!StringUtil.isEmptyOrSpaces(versionSpec)) {
      cmd.add("-Version");
      cmd.add(versionSpec);
    }

    return myExec.executeNuGet(cmd, new ListPackagesCommandProcessor(source));
  }

}
