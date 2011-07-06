package jetbrains.buildServer.nuget.server;

import java.util.Map;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * Created Eugene Petrenko (eugene.petrenko@gmail.com)
 * date: 28.04.11
 */
public class NuGetPublishRunType extends RunType {
  private final PluginDescriptor myPluginDescriptor;

  public NuGetPublishRunType(@NotNull final PluginDescriptor pluginDescriptor,
                             @NotNull final RunTypeRegistry registry) {
    myPluginDescriptor = pluginDescriptor;
    registry.registerRunType(this);
  }

  @Override
  public String getEditRunnerParamsJspFilePath() {
    return myPluginDescriptor.getPluginResourcesPath("editNuGet.jsp");
  }

  @Override
  public String getViewRunnerParamsJspFilePath() {
    return null;
  }

  @NotNull
  @Override
  public String getType() {
    return "NuGet";
  }

  @Override
  public String getDisplayName() {
    return "NuGet Package";
  }

  @Override
  public String getDescription() {
    return "Generates NuGet package from a given package specification (.nuspec) file";
  }

  @Override
  public PropertiesProcessor getRunnerPropertiesProcessor() {
    return null;
  }

  @Override
  public Map<String, String> getDefaultRunnerProperties() {
    return null;
  }
}
