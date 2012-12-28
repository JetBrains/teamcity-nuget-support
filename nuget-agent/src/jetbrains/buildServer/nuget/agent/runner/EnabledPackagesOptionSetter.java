package jetbrains.buildServer.nuget.agent.runner;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created 28.12.12 18:44
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class EnabledPackagesOptionSetter {
  public static final String ENABLE_NUGET_PACKAGE_RESTORE = "EnableNuGetPackageRestore";

  public EnabledPackagesOptionSetter(@NotNull EventDispatcher<AgentLifeCycleListener> events) {
    final Set<String> nugetTypes = new HashSet<String>(Arrays.asList(PackagesConstants.ALL_NUGET_RUN_TYPES));

    events.addListener(new AgentLifeCycleAdapter(){
      @Override
      public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        if (!nugetTypes.contains(runner.getRunType())) return;
        if (runner.getBuildParameters().getEnvironmentVariables().containsKey(ENABLE_NUGET_PACKAGE_RESTORE)) return;

        runner.addEnvironmentVariable(ENABLE_NUGET_PACKAGE_RESTORE, "True");
      }
    });
  }
}
