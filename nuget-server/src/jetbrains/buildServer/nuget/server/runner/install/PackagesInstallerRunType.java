

package jetbrains.buildServer.nuget.server.runner.install;

import jetbrains.buildServer.nuget.server.runner.NuGetRunType;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.tools.ServerToolManager;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.nuget.common.PackagesConstants.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 08.07.11 20:45
 */
public class PackagesInstallerRunType extends NuGetRunType {
  @NotNull private final PackagesInstallerRunnerDefaults myDefaults;

  public PackagesInstallerRunType(@NotNull final PluginDescriptor descriptor,
                                  @NotNull final PackagesInstallerRunnerDefaults defaults,
                                  @NotNull final ServerToolManager toolManager,
                                  @NotNull final ProjectManager projectManager) {
    super(descriptor, toolManager, projectManager);
    myDefaults = defaults;
  }

  @NotNull
  @Override
  public String getType() {
    return INSTALL_RUN_TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "NuGet Installer";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Installs and updates missing NuGet packages";
  }

  @NotNull
  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return properties -> {
      List<InvalidProperty> checks = new ArrayList<>();

      if (StringUtil.isEmptyOrSpaces(properties.get(NUGET_PATH))) {
        checks.add(new InvalidProperty(NUGET_PATH, "The path to nuget.exe must be specified"));
      }

      String sln = properties.get(SLN_PATH);
      if (StringUtil.isEmptyOrSpaces(sln)) {
        checks.add(new InvalidProperty(SLN_PATH, "The path to the Visual Studio solution file should be specified"));
      } else if (!ReferencesResolverUtil.containsReference(sln, new String[0], true) && !sln.toLowerCase().endsWith(".sln")) {
        checks.add(new InvalidProperty(SLN_PATH, "The file extension must be .sln. Specify the path to a Visual Studio solution file"));
      }

      return checks;
    };
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull Map<String, String> parameters) {
    return "Solution: " + parameters.get(SLN_PATH);
  }

  @NotNull
  @Override
  protected String getEditJsp() {
    return "install/editInstallPackage.jsp";
  }

  @NotNull
  @Override
  protected String getViewJsp() {
    return "install/viewInstallPackage.jsp";
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return myDefaults.getRunnerProperties();
  }
}
