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

package jetbrains.buildServer.nuget.agent.runner.install.impl.locate;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlXppAbstractParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 14:02
 */
public class ResourcesConfigPackagesScanner implements PackagesConfigScanner {
  private static final Logger LOG = Logger.getInstance(ResourcesConfigPackagesScanner.class.getName());

  @NotNull
  public Collection<File> scanResourceConfig(@NotNull final BuildProgressLogger logger,
                                             @NotNull final File sln,
                                             @NotNull final File packages) throws RunBuildException {
    final File repositoriesConfig = new File(packages, "repositories.config");
    LOG.debug("resources.config path is " + repositoriesConfig);

    if (!repositoriesConfig.isFile()) {
      logger.message("Failed to find repositories.config at " + repositoriesConfig);
      return Collections.emptyList();
    }

    logger.message("Found list of packages.config files: " + repositoriesConfig);
    return listPackagesConfigs(repositoriesConfig);
  }


  @NotNull
  private Collection<File> listPackagesConfigs(@NotNull final File repositoriesConfig) throws RunBuildException {
    final Collection<File> files = new ArrayList<File>();
    try {
      new XmlXppAbstractParser() {
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
