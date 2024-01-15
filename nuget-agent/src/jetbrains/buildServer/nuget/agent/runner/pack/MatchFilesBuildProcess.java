

package jetbrains.buildServer.nuget.agent.runner.pack;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.util.MatchFilesBuildProcessBase;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 09.10.11 23:02
 */
public class MatchFilesBuildProcess extends MatchFilesBuildProcessBase {
  private final NuGetPackParameters myParameters;

  public MatchFilesBuildProcess(@NotNull final BuildRunnerContext context,
                                @NotNull final NuGetPackParameters parameters,
                                @NotNull final Callback callback) {
    super(context, callback);
    myParameters = parameters;
  }

  @NotNull
  @Override
  protected Collection<String> getFiles() throws RunBuildException {
    return myParameters.getSpecFiles();
  }

  @NotNull
  @Override
  protected String getActionName() {
    return "create packages";
  }
}
