

package jetbrains.buildServer.nuget.server;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

  @NotNull
  @Override
  public String getDisplayName() {
    return "NuGet Package";
  }

  @NotNull
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

  @NotNull
  public Set<String> getTags() {
    return new HashSet<>(Arrays.asList(".NET", "NuGet"));
  }

  @Nullable
  @Override
  public String getIconUrl() {
    return myPluginDescriptor.getPluginResourcesPath("nuget-runner.svg");
  }
}
