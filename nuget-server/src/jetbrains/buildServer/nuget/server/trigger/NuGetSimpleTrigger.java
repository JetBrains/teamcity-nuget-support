package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.buildTriggers.*;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckerSettings;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 03.05.11 15:25
 */
public class NuGetSimpleTrigger extends BuildTriggerService {
  @NotNull private final NuGetTriggerController myController;
  @NotNull private final NamedPackagesUpdateChecker myChecker;
  @NotNull private final PackageCheckerSettings mySettings;

  public NuGetSimpleTrigger(@NotNull final NuGetTriggerController controller,
                            @NotNull final NamedPackagesUpdateChecker checker,
                            @NotNull final PackageCheckerSettings settings) {
    myController = controller;
    myChecker = checker;
    mySettings = settings;
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
    Map<String, String> props = trigger.getProperties();
    StringBuilder sb = new StringBuilder();
    sb.append("Package Id: ").append(props.get(TriggerConstants.PACKAGE)).append("\n");
    String version = props.get(TriggerConstants.VERSION);
    if (!StringUtil.isEmptyOrSpaces(version)) {
      sb.append("\n");
      sb.append("Version: ").append(version);
    }

    return sb.toString();
  }

  @NotNull
  @Override
  public BuildTriggeringPolicy getBuildTriggeringPolicy() {
    return new PolledBuildTrigger() {

      @Override
      public int getPollInterval(@NotNull PolledTriggerContext context) {
        return (int)(mySettings.getTriggerPollInterval()/1000);
      }

      @Override
      public void triggerBuild(@NotNull PolledTriggerContext context) throws BuildTriggerException {
        final BuildStartReason result = myChecker.checkChanges(context.getTriggerDescriptor(), context.getCustomDataStorage());

        if (result != null) {
          context.getBuildType().addToQueue(result.getReason());
        }
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

        if (StringUtil.isEmptyOrSpaces(properties.get(TriggerConstants.NUGET_EXE))) {
          err.add(new InvalidProperty(TriggerConstants.NUGET_EXE, "NuGet.exe path must be specified"));
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
    return myController.getPath();
  }
}
