package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.buildTriggers.*;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 03.05.11 15:25
 */
public class NuGetSimpleTrigger extends BuildTriggerService {
  private final PluginDescriptor myDescriptor;

  public NuGetSimpleTrigger(@NotNull final PluginDescriptor descriptor) {
    myDescriptor = descriptor;
  }

  @NotNull
  @Override
  public String getName() {
    return TriggerConstants.TRIGGER_ID;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "NuGet Dependency Trigger";
  }

  @NotNull
  @Override
  public String describeTrigger(@NotNull final BuildTriggerDescriptor trigger) {
    return "Triggers build on NuGet dependency change";
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
  public boolean isMultipleTriggersPerBuildTypeAllowed() {
    return true;
  }

  @Override
  public PropertiesProcessor getTriggerPropertiesProcessor() {
    return new PropertiesProcessor() {
      public Collection<InvalidProperty> process(Map<String, String> properties) {
        Collection<InvalidProperty> err = new ArrayList<InvalidProperty>();

        if (StringUtil.isEmptyOrSpaces(properties.get(TriggerConstants.SOURCE))) {
          err.add(new InvalidProperty(TriggerConstants.SOURCE, "Source must be specified"));
        }

        if (StringUtil.isEmptyOrSpaces(properties.get(TriggerConstants.PACKAGE))) {
          err.add(new InvalidProperty(TriggerConstants.PACKAGE, "Package Id must be specified"));
        }

        return err;
      }
    };
  }

  @Override
  public String getEditParametersUrl() {
    return myDescriptor.getPluginResourcesPath("trigger/editSimpleTrigger.jsp");
  }
}
