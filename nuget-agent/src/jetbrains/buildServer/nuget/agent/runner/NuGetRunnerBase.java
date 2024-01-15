

package jetbrains.buildServer.nuget.agent.runner;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.parameters.PackagesParametersFactory;
import jetbrains.buildServer.nuget.common.DotNetConstants;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 18:32
 */
public abstract class NuGetRunnerBase implements AgentBuildRunner, AgentBuildRunnerInfo {
  private final Pattern ourRequirementsPattern = Pattern.compile("^(" + DotNetConstants.DOTNET4VERSION_PATTERN + "|" + DotNetConstants.MONO_VERSION_PATTERN + ")$");
  protected final Logger LOG = Logger.getInstance(getClass().getName());

  protected final NuGetActionFactory myActionFactory;
  protected final PackagesParametersFactory myParametersFactory;

  public NuGetRunnerBase(NuGetActionFactory actionFactory, PackagesParametersFactory parametersFactory) {
    myActionFactory = actionFactory;
    myParametersFactory = parametersFactory;
  }

  @NotNull
  public AgentBuildRunnerInfo getRunnerInfo() {
    return this;
  }

  @NotNull
  public abstract String getType();

  public boolean canRun(@NotNull BuildAgentConfiguration agentConfiguration) {
    if(CollectionsUtil.contains(agentConfiguration.getConfigurationParameters().keySet(), new Filter<String>() {
      public boolean accept(@NotNull String data) {
        return ourRequirementsPattern.matcher(data).find();
      }
    })) return true;

    LOG.info("NuGet requires .NET Framework (x86) 4.0 and higher or Mono 3.2 and higher to be installed.");
    return false;
  }
}
