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

package jetbrains.buildServer.nuget.agent.runner.install.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 01.12.11 18:16
 */
public class RepositoryPathResolverImpl implements RepositoryPathResolver {
  private static final Logger LOG = Logger.getInstance(RepositoryPathResolverImpl.class.getName());
  private static final String NUGET_CONFIG = "nuget.config";
  private static final String DOT_NUGET = ".nuget";
  private static final String PACKAGES = "packages";

  @NotNull
  public File resolveRepositoryPath(@NotNull final BuildProgressLogger logger,
                                    @NotNull final File solutionFile,
                                    @NotNull final File workingDirectory) {
    final File repositoryPath = resolveRepositoryPathImpl(solutionFile, workingDirectory, logger);
    //noinspection ResultOfMethodCallIgnored
    repositoryPath.mkdirs();
    if (!repositoryPath.isDirectory()) {
      logger.warning("Failed to create packages directory: " + repositoryPath);
    }
    return repositoryPath;
  }

  @NotNull
  private File resolveRepositoryPathImpl(File solutionFile, File workingDirectory, BuildProgressLogger logger) {
    final File solutionHomeDir = solutionFile.getParentFile();
    final File defaultRepositoryPath = new File(solutionHomeDir, PACKAGES);

    final File configFilePath = locateNugetConfigNearSolutionFile(solutionHomeDir);
    if (configFilePath == null) {
      LOG.warn("Failed to find NuGet.config file near solution " + solutionFile + ". Packages will be downloaded into default path: " + defaultRepositoryPath + ".");
      return defaultRepositoryPath;
    }
    LOG.debug("Found NuGet.config file: " + configFilePath);

    final String repositoryPath;
    try {
      repositoryPath = extractRepositoryPathFromConfig(configFilePath);
    } catch (final Exception e) {
      final String message = "Error occured while parsing NuGet.config file at " + configFilePath + ". Packages will be downloaded into default path: " + defaultRepositoryPath + ".";
      LOG.warn(message, e);
      logger.warning(message + " " + e.getMessage());
      return defaultRepositoryPath;
    }
    if(repositoryPath == null){
      final String message = "RepositoryPath was not extracted from NuGet.config file at " + configFilePath + ". Packages will be downloaded into default path: " + defaultRepositoryPath + ".";
      LOG.info(message);
      logger.message(message);
      return defaultRepositoryPath;
    }

    return FileUtil.resolvePath(workingDirectory, repositoryPath);
  }

  @Nullable
  private File locateNugetConfigNearSolutionFile(@NotNull final File solutionHomeDir) {
    File cuirrentDir = new File(solutionHomeDir, DOT_NUGET);
    while (cuirrentDir != null){
      final File result = new File(cuirrentDir, NUGET_CONFIG);
      if(result.isFile()) return result;
      LOG.debug("NuGet.config file not found on path " + result + ".");
      cuirrentDir = cuirrentDir.getParentFile();
    }
    return null;
  }

  @Nullable
  private String extractRepositoryPathFromConfig(@NotNull final File configFilePath) throws JDOMException, IOException {
    final Element element = FileUtil.parseDocument(configFilePath);
    {
      final Attribute pathAttribute = (Attribute) XPath.newInstance("/configuration/config/add[@key='repositoryPath']/@value").selectSingleNode(element);
      if (pathAttribute != null) {
        String text = pathAttribute.getValue().trim();
        LOG.info("Found packages path: " + text);
        return text;
      }
    }

    {
      final Text pathText = (Text) XPath.newInstance("/configuration/repositoryPath/text()").selectSingleNode(element);
      if (pathText != null) {
        final String text = pathText.getTextTrim();
        LOG.info("Found repositoryPath: " + text);
        return text;
      }
    }

    {
      final Text pathText = (Text) XPath.newInstance("/settings/repositoryPath/text()").selectSingleNode(element);
      if (pathText != null) {
        final String text = pathText.getTextTrim();
        LOG.info("Found repositoryPath: " + text);
        return text;
      }
    }
    return null;
  }
}
