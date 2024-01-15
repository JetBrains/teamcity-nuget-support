

package jetbrains.buildServer.nuget.agent.dependencies.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.agent.dependencies.NuGetPackagesCollector;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlXppAbstractParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 22:45
 */
public class NuGetPackagesConfigParser {
  private static final Logger LOG = Logger.getInstance(NuGetPackagesConfigParser.class.getName());

  public void parseNuGetPackages(@NotNull final File packagesConfig,
                                 @NotNull final NuGetPackagesCollector callback) throws IOException {
    XmlXppAbstractParser parser = new XmlXppAbstractParser() {
      @Override
      protected List<XmlHandler> getRootHandlers() {
        return Arrays.asList(elementsPath(new Handler() {
          public XmlReturn processElement(@NotNull XmlElementInfo xmlElementInfo) {
            String id = xmlElementInfo.getAttribute("id");
            String version = xmlElementInfo.getAttribute("version");
            if (id == null || StringUtil.isEmptyOrSpaces(id))
              return xmlElementInfo.noDeep();

            if (version == null || StringUtil.isEmptyOrSpaces(version))
              return xmlElementInfo.noDeep();


            callback.addDependenyPackage(
                    id,
                    version,
                    xmlElementInfo.getAttribute("allowedVersions"));

            return xmlElementInfo.noDeep();
          }
        }, "packages", "package"));
      }
    };
    try {
      parser.parse(packagesConfig);
    } catch (IOException e) {
      LOG.warnAndDebugDetails("Failed to parse packages.config file: " + packagesConfig + ". " + e.getMessage(), e);
    }
  }
}
