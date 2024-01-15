

package jetbrains.buildServer.nuget.agent.commands;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 18:07
 */
public interface NuGetActionFactory {
  @NotNull
  BuildProcess createInstall(@NotNull BuildRunnerContext context,
                             @NotNull PackagesInstallParameters params,
                             @NotNull File packagesConfig,
                             @NotNull File targetFolder) throws RunBuildException;

  @NotNull
  BuildProcess createRestoreForSolution(@NotNull BuildRunnerContext context,
                                        @NotNull PackagesInstallParameters params,
                                        @NotNull File solutionFile) throws RunBuildException;

  @NotNull
  BuildProcess createUpdate(@NotNull BuildRunnerContext context,
                            @NotNull PackagesUpdateParameters params,
                            @NotNull File packagesConfig,
                            @NotNull File targetFolder) throws RunBuildException;

  @NotNull
  BuildProcess createUsageReport(@NotNull BuildRunnerContext context,
                                 @NotNull File packagesConfig) throws RunBuildException;

  @NotNull
  BuildProcess createCreatedPackagesReport(@NotNull BuildRunnerContext context,
                                           @NotNull Collection<File> packageFiles) throws RunBuildException;

  @NotNull
  BuildProcess createPublishedPackageReport(@NotNull BuildRunnerContext context,
                                            @NotNull NuGetPublishParameters params,
                                            @NotNull File packageFile) throws RunBuildException;


  @NotNull
  BuildProcess createPush(@NotNull BuildRunnerContext context,
                          @NotNull NuGetPublishParameters params,
                          @NotNull File packagePath) throws RunBuildException;

  @NotNull
  BuildProcess createPack(@NotNull BuildRunnerContext context,
                          @NotNull File specFile,
                          @NotNull NuGetPackParameters params) throws RunBuildException;
}
