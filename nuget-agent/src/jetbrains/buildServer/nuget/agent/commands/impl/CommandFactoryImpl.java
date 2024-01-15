

package jetbrains.buildServer.nuget.agent.commands.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.commands.CommandFactory;
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.nuget.agent.runner.EnabledPackagesOptionSetter;
import jetbrains.buildServer.nuget.common.PackagesPackDirectoryMode;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 16:10
 */
public class CommandFactoryImpl implements CommandFactory {

  @NotNull
  public <T> T createInstall(@NotNull PackagesInstallParameters params, @NotNull File packagesConfig, @NotNull File outputDir, @NotNull Callback<T> factory) throws RunBuildException {
    final List<String> argz = new ArrayList<String>();
    argz.add("install");
    argz.add(FileUtil.getCanonicalFile(packagesConfig).getPath()); //path to package
    if (params.getExcludeVersion()) {
      argz.add("-ExcludeVersion");
    }
    if (params.getNoCache()) {
      argz.add("-NoCache");
    }
    argz.add("-OutputDirectory");
    argz.add(FileUtil.getCanonicalFile(outputDir).getPath());

    final NuGetFetchParameters nugetFetchParams = params.getNuGetParameters();
    argz.addAll(nugetFetchParams.getCustomCommandline());

    return executeNuGet(nugetFetchParams, nugetFetchParams.getNuGetPackageSources(), argz, packagesConfig.getParentFile(), Collections.singletonMap(EnabledPackagesOptionSetter.ENABLE_NUGET_PACKAGE_RESTORE, "True"), factory);
  }

  @NotNull
  public <T> T createRestoreForSolution(@NotNull PackagesInstallParameters params, @NotNull File solutionFile, @NotNull Callback<T> factory) throws RunBuildException {
    final List<String> argz = new ArrayList<String>();
    argz.add("restore");
    argz.add(FileUtil.getCanonicalFile(solutionFile).getPath());
    if (params.getNoCache()) {
      argz.add("-NoCache");
    }

    final NuGetFetchParameters nugetFetchParams = params.getNuGetParameters();
    argz.addAll(nugetFetchParams.getCustomCommandline());

    return executeNuGet(nugetFetchParams, nugetFetchParams.getNuGetPackageSources(), argz, solutionFile.getParentFile(), factory);
  }

  @NotNull
  public <T> T createUpdate(@NotNull PackagesUpdateParameters params, @NotNull File packagesConfig, @NotNull File targetFolder, @NotNull Callback<T> factory) throws RunBuildException {
    final List<String> argz = new ArrayList<String>();
    argz.add("update");
    argz.add(FileUtil.getCanonicalFile(packagesConfig).getPath()); //path to package
    if (params.getUseSafeUpdate()) {
      argz.add("-Safe");
    }
    if (params.getIncludePrereleasePackages()) {
      argz.add("-Prerelease");
    }

    argz.addAll(params.getCustomCommandline());

    argz.add("-RepositoryPath");
    argz.add(FileUtil.getCanonicalFile(targetFolder).getPath());

    for (String id : params.getPackagesToUpdate()) {
      argz.add("-Id");
      argz.add(id);
    }

    final NuGetFetchParameters nuget = params.getNuGetParameters();
    return executeNuGet(nuget, nuget.getNuGetPackageSources(), argz, packagesConfig.getParentFile(), factory);
  }

  private void resolveBaseDirectory(@NotNull final NuGetPackParameters params,
                                    @NotNull final File specFile,
                                    @NotNull final List<String> arguments) throws RunBuildException {
    final PackagesPackDirectoryMode mode = params.getBaseDirectoryMode();
    switch(mode) {
      case LEAVE_AS_IS:
        //No -BasePath
        return;
      case EXPLICIT_DIRECTORY:
        arguments.add("-BasePath");
        arguments.add(params.getBaseDirectory().getPath());
        return;
      case PROJECT_DIRECTORY:
        arguments.add("-BasePath");
        arguments.add(specFile.getParentFile().getPath());
        return;
      default:
        throw new RunBuildException("Unexpected BaseDirectory mode: " + mode);
    }
  }

  public <T> T createPack(@NotNull final File specFile,
                          @NotNull final NuGetPackParameters params,
                          @NotNull final Callback<T> factory) throws RunBuildException {
    final List<String> arguments = new ArrayList<String>();
    arguments.add("pack");
    arguments.add(specFile.getPath());

    arguments.add("-OutputDirectory");
    arguments.add(params.getOutputDirectory().getPath());

    resolveBaseDirectory(params, specFile, arguments);

    String version = params.getVersion();
    if (!StringUtil.isEmptyOrSpaces(version)) {
      arguments.add("-Version");
      arguments.add(version);
    }

    for (String exclude : params.getExclude()) {
      arguments.add("-Exclude");
      arguments.add(exclude);
    }

    if (params.packSymbols()) {
      arguments.add("-Symbols");
    }

    if (params.packTool()) {
      arguments.add("-Tool");
    }

    for (String prop : params.getProperties()) {
      arguments.add("-Properties");
      arguments.add(prop);
    }

    //TODO: check if -Build was added
    arguments.addAll(params.getCustomCommandline());

    return executeNuGet(params, Collections.<String>emptyList(), arguments, specFile.getParentFile(), factory);
  }

  @NotNull
  public <T> T createPush(@NotNull final NuGetPublishParameters params,
                          @NotNull final File packagePath,
                          @NotNull final Callback<T> factory) throws RunBuildException {
    final List<String> arguments = new ArrayList<String>();
    arguments.add("push");
    arguments.add(packagePath.getPath());

    final String apiKey = params.getApiKey();
    if(!StringUtil.isEmptyOrSpaces(apiKey)){
      arguments.add(apiKey);
    }

    arguments.addAll(params.getCustomCommandline());

    final String source = params.getPublishSource();
    final List<String> sources = StringUtil.isEmptyOrSpaces(source)
            ? Collections.<String>emptyList()
            : Collections.singletonList(source);

    return executeNuGet(
            params,
            sources,
            arguments,
            packagePath.getParentFile(),
            Collections.<String, String>emptyMap(),
            factory);
  }

  private <T> T executeNuGet(@NotNull final NuGetParameters nuget,
                             @NotNull final Collection<String> sources,
                             @NotNull final Collection<String> arguments,
                             @NotNull final File workingDir,
                             @NotNull final Callback<T> factory) throws RunBuildException {
    return executeNuGet(nuget, sources, arguments, workingDir, Collections.<String,String>emptyMap(), factory);
  }

  private <T> T executeNuGet(@NotNull final NuGetParameters nuget,
                             @NotNull final Collection<String> sources,
                             @NotNull final Collection<String> arguments,
                             @NotNull final File workingDir,
                             @NotNull final Map<String,String> additionalEnvironment,
                             @NotNull final Callback<T> factory) throws RunBuildException {
    final List<String> argz = new ArrayList<String>(arguments);
    for (String source : sources) {
      argz.add("-Source");
      argz.add(source);
    }

    return factory.createCommand(
            nuget.getNuGetExeFile(),
            workingDir,
            argz,
            additionalEnvironment
    );
  }

}
