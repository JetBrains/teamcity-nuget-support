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

package jetbrains.buildServer.nuget.agent.runner.install.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 01.12.11 18:16
 */
public class RepositoryPathResolverImpl implements RepositoryPathResolver {
  private static final Logger LOG = Logger.getInstance(RepositoryPathResolverImpl.class.getName());

  @NotNull
  public File resolvePath(@NotNull final BuildProgressLogger logger,
                          @NotNull final File solutionFile) {
    final File home = solutionFile.getParentFile();
    final File config = new File(home, "nuget.config");

    final File defaultPath = new File(home, "packages");
    if (!config.isFile()) {
      return defaultPath;
    }

    LOG.debug("Found NuGet.config file: " + config);
    try {
      final Element element = FileUtil.parseDocument(config);
      final Element child = element.getChild("repositoryPath");
      if (child != null) {
        String text = child.getText();
        if (text != null) {
          text = text.trim();
          LOG.info("Found packages path: " + text);
          return FileUtil.resolvePath(home, text);
        }
      }
    } catch (final Exception e) {
      final String message = "Failed to parse NuGet.config file at " + config + ". Packages will be downloaded into default path: " + defaultPath + ". " + e.getMessage();
      logger.warning(message);
      LOG.warn(message, e);
      return defaultPath;
    }

    return defaultPath;
  }
}
