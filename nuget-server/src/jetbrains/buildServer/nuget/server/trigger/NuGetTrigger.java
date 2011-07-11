package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.buildTriggers.*;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 03.05.11 15:25
 */
public class NuGetTrigger extends BuildTriggerService {
  private final PluginDescriptor myDescriptor;

  public NuGetTrigger(final PluginDescriptor descriptor) {
    myDescriptor = descriptor;
  }

  @NotNull
  @Override
  public String getName() {
    return "NuGet";
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "NuGet Dependency Trigger";
  }

  @NotNull
  @Override
  public String describeTrigger(@NotNull final BuildTriggerDescriptor trigger) {
    return "Trigger build on NuGet dependency change";
  }

  @NotNull
  @Override
  public BuildTriggeringPolicy getBuildTriggeringPolicy() {
    return new PolledBuildTrigger() {
      @Override
      public void triggerBuild(@NotNull final PolledTriggerContext context) throws BuildTriggerException {
      }
    };
  }

  @Override
  public String getEditParametersUrl() {
    return myDescriptor.getPluginResourcesPath("NuGetTrigger.jsp");
  }
}
