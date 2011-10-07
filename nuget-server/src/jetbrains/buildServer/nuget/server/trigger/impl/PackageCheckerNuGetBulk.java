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
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.MultiMap;
import org.jetbrains.annotations.NotNull;

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
public class PackageCheckerNuGetBulk extends PackageCheckerNuGetBase implements PackageChecker {
  private static final Logger LOG = Logger.getInstance(PackageCheckerNuGetBulk.class.getName());

  private final ListPackagesCommand myCommand;
  private final PackageCheckerSettings mySettings;

  public PackageCheckerNuGetBulk(@NotNull final ListPackagesCommand command,
                                 @NotNull final NuGetPathCalculator toolManager,
                                 @NotNull final PackageCheckerSettings settings) {
    super(toolManager);
    myCommand = command;
    mySettings = settings;
  }

  public boolean accept(@NotNull PackageCheckRequest request) {
    return mySettings.allowBulkMode(request) && super.accept(request);
  }

  public void update(@NotNull ExecutorService executor, @NotNull Collection<CheckablePackage> data) {
    final MultiMap<File, CheckablePackage> entries = new MultiMap<File, CheckablePackage>();
    for (CheckablePackage entry : data) {
      entries.putValue(getNuGetPath(entry.getMode()), entry);
    }

    for (Map.Entry<File, List<CheckablePackage>> nuget : entries.entrySet()) {
      final Map<SourcePackageReference, CheckablePackage> map = new HashMap<SourcePackageReference, CheckablePackage>();
      for (CheckablePackage e : nuget.getValue()) {
        map.put(e.getPackage(), e);
        e.setExecuting();
      }

      final File nugetPath = nuget.getKey();

      executor.submit(ExceptionUtil.catchAll("Bulk check for update of NuGet packages", new Runnable() {
        public void run() {
          try {
            final Map<SourcePackageReference, Collection<SourcePackageInfo>> result = myCommand.checkForChanges(nugetPath, map.keySet());

            for (Map.Entry<SourcePackageReference, Collection<SourcePackageInfo>> e : result.entrySet()) {
              final SourcePackageReference ref = e.getKey();
              final CheckablePackage p = map.get(ref);
              if (p != null) {
                p.setResult(CheckResult.succeeded(e.getValue()));
                map.remove(ref);
              }
            }

            for (CheckablePackage entry : map.values()) {
              LOG.warn("No information returned for package: " + entry.getPackage());
              entry.setResult(CheckResult.failed("No information returned from bulk command"));
            }

          } catch (Throwable t) {
            LOG.warn("Failed to bulk check changes of NuGet packages. " + t.getMessage(), t);
            for (CheckablePackage entry : map.values()) {
              entry.setResult(CheckResult.failed(t.getMessage()));
            }
          }
        }
      }));
    }
  }
}
