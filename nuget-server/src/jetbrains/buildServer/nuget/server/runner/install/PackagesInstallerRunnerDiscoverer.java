

package jetbrains.buildServer.nuget.server.runner.install;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.StreamUtil;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.tool.NuGetServerToolProvider;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.tools.ToolVersionReference;
import jetbrains.buildServer.util.*;
import jetbrains.buildServer.util.browser.Browser;
import jetbrains.buildServer.util.browser.Element;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Evgeniy.Koshkin
 */
public class PackagesInstallerRunnerDiscoverer extends BreadthFirstRunnerDiscoveryExtension {

  private static final Logger LOG = Logger.getInstance(PackagesInstallerRunnerDiscoverer.class.getName());
  private static final String PACKAGES_CONFIG = "packages.config";
  private static final String NUGET_DIR_NAME = ".nuget";
  private static final String SLN_FILE_EXTENSION = ".sln";
  private static final Pattern PROJECT_PATH_PATTERN = Pattern.compile("^Project\\(.+\\)\\s*=\\s*\".+\"\\s*,\\s*\"(.+)\"\\s*,\\s*\".+\"\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("^.+\\.(proj|csproj|vbproj)$", Pattern.CASE_INSENSITIVE);

  @NotNull private final PackagesInstallerRunnerDefaults myDefaults;

  public PackagesInstallerRunnerDiscoverer(@NotNull PackagesInstallerRunnerDefaults defaults) {
    myDefaults = defaults;
  }

  @NotNull
  @Override
  protected List<DiscoveredObject> discoverRunnersInDirectory(@NotNull Element dir, @NotNull List<Element> filesAndDirs) {
    List<String> foundSlns = new ArrayList<String>();
    boolean nugetUsageFound = false;

    for(Element item : filesAndDirs){
      final String name = item.getName();
      final boolean isLeaf = item.isLeaf();

      if(isLeaf && name.endsWith(SLN_FILE_EXTENSION) && item.isContentAvailable()) {
        foundSlns.add(item.getFullName());
        if (!nugetUsageFound && hasPackages(item)) {
          nugetUsageFound = true;
        }
      }

      if(nugetUsageFound) continue;
      nugetUsageFound = (isLeaf && name.equalsIgnoreCase(PACKAGES_CONFIG)) || (!isLeaf && name.equalsIgnoreCase(NUGET_DIR_NAME));
    }

    if (foundSlns.isEmpty() || !nugetUsageFound) return Collections.emptyList();

    return CollectionsUtil.convertCollection(foundSlns, this::discover);
  }

  private boolean hasPackages(Element solutionFile) {
    InputStream stream = null;
    try {
      stream = solutionFile.getInputStream();
      final String text = StreamUtil.readText(stream);
      for (String line : text.split("\\r?\\n")) {
        final Matcher matcher = PROJECT_PATH_PATTERN.matcher(line);
        if (!matcher.find()) {
          continue;
        }

        final String projectFilePath = matcher.group(1);
        if (!PROJECT_NAME_PATTERN.matcher(projectFilePath).find()) {
          continue;
        }

        // Check packages.config file existence in the project file directory
        final String packagesPath = FileUtil.normalizeRelativePath(solutionFile.getFullName() + "/../" + projectFilePath + "/../" + PACKAGES_CONFIG);
        final Element packagesElement = solutionFile.getBrowser().getElement(packagesPath);
        if (packagesElement != null && packagesElement.isContentAvailable()) {
          return true;
        }

        // Check that project file contains PackageReferences
        final String projectPath = FileUtil.normalizeRelativePath(solutionFile.getFullName() + "/../" + projectFilePath);
        if (hasProjectReferences(solutionFile.getBrowser().getElement(projectPath))) {
          return true;
        }
      }
    } catch (IOException e) {
      LOG.infoAndDebugDetails("Failed to read solution contents " + solutionFile.getFullName(), e);
    } finally {
      FileUtil.close(stream);
    }

    return false;
  }

  private boolean hasProjectReferences(Element projectFile) {
    if (projectFile == null || !projectFile.isContentAvailable()) {
      return false;
    }

    InputStream stream = null;
    try {
      stream = projectFile.getInputStream();
      return StreamUtil.readText(stream).contains("PackageReference");
    } catch (IOException e) {
      LOG.infoAndDebugDetails("Failed to read project file contents " + projectFile.getFullName(), e);
    } finally {
      FileUtil.close(stream);
    }

    return false;
  }

  @NotNull
  @Override
  protected List<DiscoveredObject> postProcessDiscoveredObjects(@NotNull BuildTypeSettings settings, @NotNull Browser browser, @NotNull List<DiscoveredObject> discovered) {
    if(discovered.isEmpty()) return discovered;

    Set<String> configuredPaths = new HashSet<String>();
    for (SBuildRunnerDescriptor r: settings.getBuildRunners()) {
      if (r.getType().equals(PackagesConstants.INSTALL_RUN_TYPE)) {
        String path = r.getParameters().get(PackagesConstants.SLN_PATH);
        if (path != null) {
          configuredPaths.add(FileUtil.toSystemIndependentName(path));
        }
      }
    }
    if (configuredPaths.isEmpty()) return discovered;

    List<DiscoveredObject> res = new ArrayList<DiscoveredObject>();
    for (DiscoveredObject obj: discovered) {
      final String slnPath = obj.getParameters().get(PackagesConstants.SLN_PATH);
      if (slnPath != null && configuredPaths.contains(FileUtil.toSystemIndependentName(slnPath))) continue;
      res.add(obj);
    }
    return res;
  }

  private DiscoveredObject discover(String slnPath) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(PackagesConstants.NUGET_PATH, ToolVersionReference.getDefaultToolReference(NuGetServerToolProvider.NUGET_TOOL_TYPE.getType()).getReference());
    parameters.put(PackagesConstants.SLN_PATH, slnPath);
    parameters.putAll(myDefaults.getRunnerProperties());
    return new DiscoveredObject(PackagesConstants.INSTALL_RUN_TYPE, parameters);
  }
}
