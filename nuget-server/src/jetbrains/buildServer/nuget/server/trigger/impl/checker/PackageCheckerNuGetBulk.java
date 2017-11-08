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

package jetbrains.buildServer.nuget.server.trigger.impl.checker;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.exec.ListPackagesResult;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckResult;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.settings.PackageCheckerSettings;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.MultiMap;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 19:31
 */
public class PackageCheckerNuGetBulk extends PackageCheckerNuGetBase implements PackageChecker {
  private static final Logger LOG = Logger.getInstance(PackageCheckerNuGetBulk.class.getName());

  private final ListPackagesCommand myCommand;
  private final PackageCheckerSettings mySettings;

  public PackageCheckerNuGetBulk(@NotNull final ListPackagesCommand command,
                                 @NotNull final PackageCheckerSettings settings) {
    myCommand = command;
    mySettings = settings;
  }

  public boolean accept(@NotNull PackageCheckRequest request) {
    return super.accept(request) && mySettings.allowBulkMode();
  }

  public void update(@NotNull ExecutorService executor, @NotNull Collection<CheckablePackage> data) {
    final MultiMap<File, CheckablePackage> entries = new MultiMap<>();
    for (CheckablePackage entry : data) {
      entries.putValue(getNuGetPath(entry.getMode()), entry);
    }

    for (Map.Entry<File, List<CheckablePackage>> nuget : entries.entrySet()) {
      final File nugetPath = nuget.getKey();

      final List<CheckablePackage> allVersions = new ArrayList<>();
      final List<CheckablePackage> setVersions = new ArrayList<>();
      for (CheckablePackage p : nuget.getValue()) {
        (p.getPackage().getVersionSpec() == null ? allVersions : setVersions).add(p);
      }

      //first schedule packages check without version, this will work faster
      chunkAndSchedule(executor, nugetPath, allVersions);

      //than schedule check for packages with versions constraint
      chunkAndSchedule(executor, nugetPath, setVersions);
    }
  }

  private void chunkAndSchedule(@NotNull ExecutorService executor,
                                @NotNull File nugetPath,
                                @NotNull Collection<CheckablePackage> requests) {
    Map<SourcePackageReference, CheckablePackage> map = new HashMap<>();
    for (CheckablePackage e : requests) {
      map.put(e.getPackage(), e);
      e.setExecuting();

      if (map.size() >= mySettings.getMaxPackagesToQueryInBulk()) {
        postCheckTask(executor, map, nugetPath);
        map = new HashMap<>();
      }
    }
    if (!map.isEmpty()) {
      postCheckTask(executor, map, nugetPath);
    }
  }

  private void postCheckTask(ExecutorService executor, Map<SourcePackageReference, CheckablePackage> _map, final File nugetPath) {
    //avoid possible concurrency here.
    final Map<SourcePackageReference, CheckablePackage> map = new HashMap<>(_map);
    executor.submit(ExceptionUtil.catchAll("Checking updates of NuGet packages", () -> {
      try {
        final Map<SourcePackageReference, ListPackagesResult> result = myCommand.checkForChanges(nugetPath, map.keySet());

        for (Map.Entry<SourcePackageReference, ListPackagesResult> e : result.entrySet()) {
          final SourcePackageReference ref = e.getKey();
          final CheckablePackage p = map.get(ref);
          if (p == null) continue;

          p.setResult(CheckResult.fromResult(e.getValue()));
          map.remove(ref);
        }

        for (CheckablePackage entry : map.values()) {
          final String msg = "Package " + entry.getPackage().getPackageId() + " was not found in the feed";
          LOG.warn(msg + ": " + entry.getPackage());
          entry.setResult(CheckResult.empty());
        }

      } catch (Throwable t) {
        LOG.warnAndDebugDetails("Failed to check updates of NuGet packages", t);
        for (CheckablePackage entry : map.values()) {
          entry.setResult(CheckResult.failed(t.getMessage()));
        }
      }
    }));
  }
}
