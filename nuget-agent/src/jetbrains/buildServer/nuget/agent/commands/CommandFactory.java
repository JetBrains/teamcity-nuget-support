

package jetbrains.buildServer.nuget.agent.commands;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 16:07
 */
public interface CommandFactory {

  @NotNull
  <T> T createInstall(@NotNull PackagesInstallParameters params,
                      @NotNull File packagesConfig,
                      @NotNull File outputDir,
                      @NotNull Callback<T> factory) throws RunBuildException;

  @NotNull
  <T> T createRestoreForSolution(@NotNull PackagesInstallParameters params,
                                 @NotNull File solutionFile,
                                 @NotNull Callback<T> factory) throws RunBuildException;

  @NotNull
  <T> T createUpdate(@NotNull PackagesUpdateParameters params,
                     @NotNull File packagesConfig,
                     @NotNull File targetFolder,
                     @NotNull Callback<T> factory) throws RunBuildException;

  @NotNull
  <T> T createPush(@NotNull NuGetPublishParameters params,
                   @NotNull File packagePath,
                   @NotNull Callback<T> factory) throws RunBuildException;

  <T> T createPack(@NotNull File specFile,
                   @NotNull NuGetPackParameters params,
                   @NotNull Callback<T> factory) throws RunBuildException;

  public interface Callback<T> {
    /**
     * Called with generated arguments to crate execurable instance or run command
     * @param program program to run
     * @param workingDir working firectory of program
     * @param argz arguments array
     * @param additionalEnvironment environment variables that has to be added
     * @return some result object depending of caller's desire
     * @throws RunBuildException if failed to create/execure command
     */
    @NotNull
    T createCommand(@NotNull File program,
                    @NotNull File workingDir,
                    @NotNull Collection<String> argz,
                    @NotNull Map<String, String> additionalEnvironment) throws RunBuildException;
  }
}
