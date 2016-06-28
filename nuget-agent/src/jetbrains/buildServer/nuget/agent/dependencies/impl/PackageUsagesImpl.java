/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.agent.dependencies.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.agent.dependencies.NuGetPackagesCollector;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.common.PackageInfoLoader;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.feedReader.NuGetPackageInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 23:17
 */
public class PackageUsagesImpl implements PackageUsages {
  private static final Logger LOG = Logger.getInstance(PackageUsagesImpl.class.getName());

  private final NuGetPackagesCollector myCollector;
  private final NuGetPackagesConfigParser myParser;
  private final PackageInfoLoader myLoader;

  public PackageUsagesImpl(@NotNull final NuGetPackagesCollector collector,
                           @NotNull final NuGetPackagesConfigParser parser,
                           @NotNull final PackageInfoLoader loader) {
    myCollector = collector;
    myParser = parser;
    myLoader = loader;
  }

  public void reportInstalledPackages(@NotNull final File packagesConfig) {
    if (!packagesConfig.exists()) {
      LOG.debug("Packages file: " + packagesConfig + " does not exit");
      return;
    }

    try {
      myParser.parseNuGetPackages(packagesConfig, myCollector);
    } catch (IOException e) {
      LOG.warn("Failed to parse " + packagesConfig + ". " + e.getMessage(), e);
    }
  }

  public void reportCreatedPackages(@NotNull final Collection<File> packageFiles) {
    for (File file : packageFiles) {
      try {
        NuGetPackageInfo info = myLoader.loadPackageInfo(file);
        myCollector.addCreatedPackage(info.getId(), info.getVersion());
      } catch (PackageLoadException e) {
        LOG.warn("Failed to parse create NuGet package: " + file);
      }
    }
  }

  public void reportPublishedPackage(@NotNull final File packageFile, @Nullable String source) {
    try {
      NuGetPackageInfo info = myLoader.loadPackageInfo(packageFile);
      myCollector.addPublishedPackage(info.getId(), info.getVersion(), source);
    } catch (PackageLoadException e) {
      LOG.warn("Failed to parse create NuGet package: " + packageFile);
    }
  }
}
