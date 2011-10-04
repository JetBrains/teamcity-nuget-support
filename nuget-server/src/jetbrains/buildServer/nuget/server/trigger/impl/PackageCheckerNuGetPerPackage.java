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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 16:51
 */
public class PackageCheckerNuGetPerPackage extends PackageCheckerNuGetBase implements PackageChecker {
  private static final Logger LOG = Logger.getInstance(PackageCheckerNuGetPerPackage.class.getName());

  private final ListPackagesCommand myCommand;
  private final PackageCheckerSettings mySettings;

  public PackageCheckerNuGetPerPackage(@NotNull final ListPackagesCommand command,
                                       @NotNull final NuGetPathCalculator toolManager,
                                       @NotNull PackageCheckerSettings settings) {
    super(toolManager);
    myCommand = command;
    mySettings = settings;
  }

  public boolean accept(@NotNull PackageCheckRequest request) {
    return !mySettings.alowBulkMode(request) && super.accept(request);
  }

  public void update(@NotNull ExecutorService executor, @NotNull Collection<CheckablePackage> data) {
    final MultiMap<File, CheckablePackage> entries = new MultiMap<File, CheckablePackage>();
    for (CheckablePackage entry : data) {
      entries.putValue(getNuGetPath(entry.getMode()), entry);
    }

    for (Map.Entry<File, List<CheckablePackage>> nuget : entries.entrySet()) {
      final File nugetPath = nuget.getKey();
      for (final CheckablePackage packageCheckEntry : nuget.getValue()) {
        packageCheckEntry.setExecuting();
        final String packageId = packageCheckEntry.getPackage().getPackageId();
        executor.submit(ExceptionUtil.catchAll("Check update of NuGet package " + packageId, new Runnable() {
          public void run() {
            try {
              final SourcePackageReference pkg = packageCheckEntry.getPackage();
              final Collection<SourcePackageInfo> infos = myCommand.checkForChanges(nugetPath, pkg);
              packageCheckEntry.setResult(CheckResult.succeeded(infos));
            } catch (Throwable t) {
              LOG.warn("Failed to check changes of " + packageId + ". " + t.getMessage(), t);
              packageCheckEntry.setResult(CheckResult.failed(t.getMessage()));
            }
          }
        }));
      }
    }
  }
}
