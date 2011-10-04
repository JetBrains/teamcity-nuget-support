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

package jetbrains.buildServer.nuget.server.trigger.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetInstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolManager;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 19:31
 */
public class PackageCheckerNuGetBulk implements PackageChecker {
  private static final Logger LOG = Logger.getInstance(PackageCheckerNuGetPerPackage.class.getName());

  private final ListPackagesCommand myCommand;
  private final PackageCheckerSettings mySettings;
  private final NuGetToolManager myToolManager;

  public PackageCheckerNuGetBulk(@NotNull final ListPackagesCommand command,
                                 @NotNull final NuGetToolManager toolManager,
                                 @NotNull final PackageCheckerSettings settings) {
    myCommand = command;
    myToolManager = toolManager;
    mySettings = settings;
  }

  public boolean accept(@NotNull PackageCheckRequest request) {
    return mySettings.alowBulkMode(request) && getNuGetPath(request) != null;
  }

  public void update(@NotNull ExecutorService executor, @NotNull Collection<PackageCheckEntry> data) {
    final MultiMap<File, PackageCheckEntry> entries = new MultiMap<File, PackageCheckEntry>();
    for (PackageCheckEntry entry : data) {
      entries.putValue(getNuGetPath(entry.getRequest()), entry);
    }

    for (Map.Entry<File, List<PackageCheckEntry>> nuget : entries.entrySet()) {
      final Map<SourcePackageReference, PackageCheckEntry> map = new HashMap<SourcePackageReference, PackageCheckEntry>();
      for (PackageCheckEntry e : nuget.getValue()) {
        map.put(e.getRequest().getPackage(), e);
        e.setExecuting();
      }

      final File nugetPath = nuget.getKey();

      executor.submit(ExceptionUtil.catchAll("Bulk check for update of NuGet packages", new Runnable() {
        public void run() {
          try {
            final Map<SourcePackageReference, Collection<SourcePackageInfo>> result = myCommand.checkForChanges(nugetPath, map.keySet());

            for (Map.Entry<SourcePackageReference, Collection<SourcePackageInfo>> e : result.entrySet()) {
              final SourcePackageReference ref = e.getKey();
              final PackageCheckEntry p = map.get(ref);
              if (p != null) {
                p.setResult(CheckResult.succeeded(e.getValue()));
                map.remove(ref);
              }
            }

            for (PackageCheckEntry entry : map.values()) {
              LOG.warn("No information returned for package: " + entry.getRequest().getPackage());
              entry.setResult(CheckResult.failed("No information returned from bulk command"));
            }

          } catch (Throwable t) {
            LOG.warn("Failed to bulk check changes of NuGet packages. " + t.getMessage(), t);
            for (PackageCheckEntry entry : map.values()) {
              entry.setResult(CheckResult.failed(t.getMessage()));
            }
          }
        }
      }));
    }
  }

  @Nullable
  private File getNuGetPath(@NotNull PackageCheckRequest entry) {
    final CheckRequestMode mode = entry.getMode();
    if (mode instanceof CheckRequestModeNuGet) {
      return ((CheckRequestModeNuGet) mode).getNuGetPath();
    }

    final NuGetInstalledTool tool = myToolManager.getLatestNuGetTool();
    if (tool != null) {
      return tool.getPath();
    }

    return null;
  }
}
