

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 18:08
 */
public interface PackagesParametersFactory {
  /**
   * Creates object-style implementation of parameters
   *
   * @param context current build step context
   * @return parameters
   * @throws RunBuildException if failed to create parameters
   */
  @NotNull
  NuGetFetchParameters loadNuGetFetchParameters(@NotNull final BuildRunnerContext context) throws RunBuildException;

  @Nullable
  PackagesInstallParameters loadInstallPackagesParameters(@NotNull final BuildRunnerContext context,
                                                          @NotNull final NuGetFetchParameters nuget) throws RunBuildException;

  @Nullable
  PackagesUpdateParameters loadUpdatePackagesParameters(@NotNull final BuildRunnerContext context,
                                                        @NotNull final NuGetFetchParameters nuget) throws RunBuildException;


  @NotNull
  NuGetPublishParameters loadPublishParameters(@NotNull final BuildRunnerContext context) throws RunBuildException;

  @NotNull
  NuGetPackParameters loadPackParameters(@NotNull final BuildRunnerContext context) throws RunBuildException;
}
