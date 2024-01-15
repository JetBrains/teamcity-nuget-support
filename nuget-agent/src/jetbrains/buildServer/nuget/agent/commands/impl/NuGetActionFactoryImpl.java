

package jetbrains.buildServer.nuget.agent.commands.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.CommandFactory;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPublishParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 17:49
 */
public class NuGetActionFactoryImpl implements NuGetActionFactory {
  private final CommandFactory myCommandFactory;
  private final CommandlineBuildProcessFactory myFactory;
  private final PackageUsages myPackageUsages;

  public NuGetActionFactoryImpl(@NotNull final CommandlineBuildProcessFactory factory,
                                @NotNull final PackageUsages packageUsages,
                                @NotNull final CommandFactory commandFactory) {
    myFactory = factory;
    myPackageUsages = packageUsages;
    myCommandFactory = commandFactory;
  }

  private CommandFactory.Callback<BuildProcess> getCallback(@NotNull final BuildRunnerContext context) {
    return new CommandFactory.Callback<BuildProcess>() {
      @NotNull
      public BuildProcess createCommand(@NotNull File program,
                                        @NotNull File workingDir,
                                        @NotNull Collection<String> _argz,
                                        @NotNull Map<String, String> additionalEnvironment) throws RunBuildException {
        if (!program.isFile()) {
          throw new RunBuildException("Failed to find NuGet executable at: " + program);
        }

        return myFactory.executeCommandLine(
                context,
                program.getPath(),
                _argz,
                workingDir,
                additionalEnvironment
        );
      }
    };
  }

  @NotNull
  public BuildProcess createInstall(@NotNull final BuildRunnerContext context,
                                    @NotNull final PackagesInstallParameters params,
                                    @NotNull final File packagesConfig,
                                    @NotNull final File targetFolder) throws RunBuildException {
    return myCommandFactory.createInstall(params, packagesConfig, targetFolder, getCallback(context));
  }

  @NotNull
  public BuildProcess createRestoreForSolution(@NotNull final BuildRunnerContext context,
                                               @NotNull final PackagesInstallParameters params,
                                               @NotNull final File solutionFile) throws RunBuildException {
    return myCommandFactory.createRestoreForSolution(params, solutionFile, getCallback(context));
  }


  @NotNull
  public BuildProcess createUpdate(@NotNull final BuildRunnerContext context,
                                   @NotNull final PackagesUpdateParameters params,
                                   @NotNull final File packagesConfig,
                                   @NotNull final File targetFolder) throws RunBuildException {
    return myCommandFactory.createUpdate(params, packagesConfig, targetFolder, getCallback(context));
  }

  @NotNull
  public BuildProcess createUsageReport(@NotNull final BuildRunnerContext context,
                                        @NotNull final File packagesConfig) throws RunBuildException {
    return new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        myPackageUsages.reportInstalledPackages(packagesConfig);
        return BuildFinishedStatus.FINISHED_SUCCESS;
      }
    };
  }

  @NotNull
  public BuildProcess createCreatedPackagesReport(@NotNull final BuildRunnerContext context,
                                                  @NotNull final Collection<File> packageFiles) throws RunBuildException {
    return new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        myPackageUsages.reportCreatedPackages(packageFiles);
        return BuildFinishedStatus.FINISHED_SUCCESS;
      }
    };
  }

  @NotNull
  public BuildProcess createPublishedPackageReport(@NotNull final BuildRunnerContext context,
                                                   @NotNull final NuGetPublishParameters params,
                                                   @NotNull final File packageFile) throws RunBuildException {
    return new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() throws RunBuildException {
        myPackageUsages.reportPublishedPackage(packageFile, params.getPublishSource());
        return BuildFinishedStatus.FINISHED_SUCCESS;
      }
    };
  }

  @NotNull
  public BuildProcess createPush(@NotNull BuildRunnerContext context,
                                 @NotNull NuGetPublishParameters params,
                                 @NotNull File packagePath) throws RunBuildException {
    return myCommandFactory.createPush(params, packagePath, getCallback(context));
  }

  @NotNull
  public BuildProcess createPack(@NotNull BuildRunnerContext context,
                                 @NotNull File specFile,
                                 @NotNull NuGetPackParameters params) throws RunBuildException {
    return myCommandFactory.createPack(
            specFile,
            params,
            getCallback(context));
  }
}
