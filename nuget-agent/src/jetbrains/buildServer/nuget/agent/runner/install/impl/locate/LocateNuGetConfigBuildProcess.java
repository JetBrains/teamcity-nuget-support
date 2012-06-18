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

package jetbrains.buildServer.nuget.agent.runner.install.impl.locate;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolver;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlXppAbstractParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 16:03
 */
public class LocateNuGetConfigBuildProcess extends BuildProcessBase {
  private static final Logger LOG = Logger.getInstance(LocateNuGetConfigBuildProcess.class.getName());

  private final NuGetFetchParameters myContext;
  private final BuildProgressLogger myLogger;
  private final RepositoryPathResolver myResolver;
  private final PackagesInstallerCallback myCallback;

  public LocateNuGetConfigBuildProcess(@NotNull final NuGetFetchParameters context,
                                       @NotNull final BuildProgressLogger logger,
                                       @NotNull final RepositoryPathResolver resolver,
                                       @NotNull final PackagesInstallerCallback callback) {
    myContext = context;
    myLogger = logger;
    myResolver = resolver;
    myCallback = callback;
  }

  @NotNull
  @Override
  protected BuildFinishedStatus waitForImpl() throws RunBuildException {
    final File sln = myContext.getSolutionFile();
    final File packages = myResolver.resolvePath(myLogger, sln);
    final File repositoriesConfig = new File(packages, "repositories.config");

    if (sln.isFile()) {
      LOG.debug("Found Visual Studio .sln file: " + sln);
      myCallback.onSolutionFileFound(sln, packages);
    }

    LOG.debug("resources.config path is " + repositoriesConfig);

    if (!repositoriesConfig.isFile()) {
      throw new RunBuildException("Failed to find repositories.config at " + repositoriesConfig);
    }

    myLogger.message("Found packages folder: " + packages);
    myLogger.message("Found list of packages.config files: " + repositoriesConfig);
    final Collection<File> files = listPackagesConfigs(repositoriesConfig);
    final File solutionPackagesConfig = findSolutionPackagesConfigFile(sln);
    if (solutionPackagesConfig != null) {
      myLogger.message("Found solution-wide packages.config: " + solutionPackagesConfig);
      files.add(solutionPackagesConfig);
    }

    if (files.isEmpty()) {
      myLogger.warning("No packages.config files were found under solution. Nothing to install");
      return BuildFinishedStatus.FINISHED_SUCCESS;
    }

    for (File file : files) {
      myCallback.onPackagesConfigFound(file, packages);
    }

    return BuildFinishedStatus.FINISHED_SUCCESS;
  }

  @Nullable
  private File findSolutionPackagesConfigFile(@NotNull final File sln) {
    final File parentFile = sln.getParentFile();
    if (parentFile == null) return null;
    final File path = new File(parentFile, ".nuget/packages.config");
    if (path.isFile()) return path;
    return null;
  }

  @NotNull
  private Collection<File> listPackagesConfigs(@NotNull final File repositoriesConfig) throws RunBuildException {
    final Collection<File> files = new ArrayList<File>();
    try {
      new XmlXppAbstractParser(){
        @Override
        protected List<XmlHandler> getRootHandlers() {
          return Arrays.asList(elementsPath(new Handler() {
            public XmlReturn processElement(@NotNull XmlElementInfo xmlElementInfo) {
              final String relPath = xmlElementInfo.getAttribute("path");
              if (relPath != null && !StringUtil.isEmptyOrSpaces(relPath)) {
                files.add(FileUtil.resolvePath(repositoriesConfig.getParentFile(), relPath));
              }
              return xmlElementInfo.noDeep();
            }
          }, "repositories", "repository"));
        }
      }.parse(repositoriesConfig);
    } catch (IOException e) {
      throw new RunBuildException("Failed to parse repositories.config at " + repositoriesConfig + ". " + e.getMessage(), e);
    }

    return files;
  }
}
