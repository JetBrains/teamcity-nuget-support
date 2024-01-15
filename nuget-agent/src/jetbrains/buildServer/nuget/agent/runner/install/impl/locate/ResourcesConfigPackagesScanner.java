

package jetbrains.buildServer.nuget.agent.runner.install.impl.locate;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.runner.install.impl.PathUtils;
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
      logger.message("No repositories.config found at " + repositoriesConfig);
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
              final String relPath = PathUtils.normalize(xmlElementInfo.getAttribute("path"));
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
