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

package jetbrains.buildServer.nuget.agent.install.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.nuget.agent.install.NuGetPackagesCollector;
import jetbrains.buildServer.nuget.agent.install.NuGetPackagesConfigParser;
import jetbrains.buildServer.nuget.agent.install.PackageUsages;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 23:17
 */
public class PackageUsagesImpl implements PackageUsages {
  private static final Logger LOG = Logger.getInstance(PackageUsagesImpl.class.getName());

  private final NuGetPackagesCollector myCollector;
  private final NuGetPackagesConfigParser myParser;

  public PackageUsagesImpl(@NotNull final NuGetPackagesCollector collector,
                           @NotNull final NuGetPackagesConfigParser parser) {
    myCollector = collector;
    myParser = parser;
  }

  @NotNull
  public BuildProcess createReport(@NotNull final File packagesConfig) {
    return new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        if (!packagesConfig.exists()) {
          LOG.debug("Packages file: " + packagesConfig + " does not exit");
          return BuildFinishedStatus.FINISHED_SUCCESS;
        }

        try {
          myParser.parseNuGetPackages(packagesConfig, myCollector);
        } catch (IOException e) {
          LOG.warn("Failed to parse " + packagesConfig + ". " + e.getMessage(), e);
        }

        return BuildFinishedStatus.FINISHED_SUCCESS;
      }
    };
  }
}
