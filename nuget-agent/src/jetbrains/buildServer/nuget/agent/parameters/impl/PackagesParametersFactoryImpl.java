/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.agent.parameters.impl;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.nuget.common.*;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static jetbrains.buildServer.nuget.common.PackagesConstants.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 18:09
 */
public class PackagesParametersFactoryImpl implements PackagesParametersFactory {
  @NotNull
  public NuGetFetchParameters loadNuGetFetchParameters(@NotNull final BuildRunnerContext context) throws RunBuildException {
    return new NuGetFetchParameters() {
      @NotNull
      public File getSolutionFile() throws RunBuildException {
        return getFile(context, SLN_PATH, "Visual Studio .sln file");
      }

      @NotNull
      public File getWorkingDirectory() {
        return context.getWorkingDirectory();
      }

      @NotNull
      public Collection<String> getCustomCommandline() {
        return getMultilineParameter(context, NUGET_RESTORE_CUSOM_COMMANDLINE, true);
      }

      @NotNull
      public File getNuGetExeFile() throws RunBuildException {
        return getPathToNuGet(context);
      }

      @NotNull
      public Collection<String> getNuGetPackageSources() {
        return getMultilineParameter(context, NUGET_SOURCES);
      }
    };
  }

  @NotNull
  private File getPathToNuGet(BuildRunnerContext context) throws RunBuildException {
    final String nugetPathParamValue = context.getRunnerParameters().get(PackagesConstants.NUGET_PATH);
    if(StringUtil.isEmpty(nugetPathParamValue)) {
      throw new RunBuildException("Path to NuGet.exe is not provided via runner parameter " + PackagesConstants.NUGET_PATH);
    }
    final File nugetPathProvided = new File(nugetPathParamValue);
    return !nugetPathProvided.isDirectory()
            ? FileUtil.resolvePath(context.getBuild().getCheckoutDirectory(), nugetPathParamValue)
            : new File(nugetPathProvided, FeedConstants.PATH_TO_NUGET_EXE);
  }

  @NotNull
  private File getDirectory(@NotNull final BuildRunnerContext context,
                            @NotNull final String runnerParameter,
                            @NotNull final String errorMessage) throws RunBuildException {
    final File file = resolveParameterPath(context, runnerParameter);
    if (!file.isDirectory()) {
      //noinspection ResultOfMethodCallIgnored
      file.mkdirs();
    }

    if (!file.isDirectory()) {
      throw new RunBuildException("Failed to find or create " + errorMessage + " at " + file);
    }

    return file;
  }

  @NotNull
  private File getFile(@NotNull final BuildRunnerContext context,
                       @NotNull final String runnerParameter,
                       @NotNull final String errorMessage) throws RunBuildException {
    final File file = resolveParameterPath(context, runnerParameter);
    if (!file.exists()) {
      throw new RunBuildException("Failed to find " + errorMessage + " at " + file);
    }

    return file;
  }

  private File resolveParameterPath(@NotNull final BuildRunnerContext context,
                                    @NotNull final String runnerParameter) throws RunBuildException {
    String path = getParameter(context, runnerParameter);
    if (path == null || StringUtil.isEmptyOrSpaces(path))
      throw new RunBuildException("Runner parameter '" + runnerParameter + "' was not found");

    return FileUtil.resolvePath(context.getBuild().getCheckoutDirectory(), path);
  }


  @NotNull
  private Collection<String> getMultilineParameter(@NotNull final BuildRunnerContext context,
                                                   @NotNull final String nugetSources) {
    return getMultilineParameter(context, nugetSources, false);
  }

  @NotNull
  private Collection<String> getMultilineParameter(@NotNull final BuildRunnerContext context,
                                                   @NotNull final String key,
                                                   final boolean spitBySpaces) {
    final String value = getParameter(context, key);
    if (StringUtil.isEmptyOrSpaces(value)) {
      return Collections.emptyList();
    }

    final List<String> list = new ArrayList<String>();
    for (String line : StringUtil.splitByLines(value)) {
      if (spitBySpaces) {
        for (String argument : StringUtil.splitCommandArgumentsAndUnquote(line)) {
          if (!StringUtil.isEmptyOrSpaces(argument)) {
            list.add(argument);
          }
        }
      } else {
        if (!StringUtil.isEmptyOrSpaces(line)) {
          list.add(line.trim());
        }
      }
    }

    return Collections.unmodifiableList(list);
  }

  private boolean getBoolean(@NotNull BuildRunnerContext context, @NotNull String key) {
    return !StringUtil.isEmptyOrSpaces(getParameter(context, key));
  }

  @Nullable
  private String getParameter(@NotNull BuildRunnerContext context, @NotNull String key) {
    return context.getRunnerParameters().get(key);
  }

  public PackagesInstallParameters loadInstallPackagesParameters(@NotNull final BuildRunnerContext context,
                                                                 @NotNull final NuGetFetchParameters nuget) throws RunBuildException {
    return new PackagesInstallParameters() {
      @NotNull
      public NuGetFetchParameters getNuGetParameters() {
        return nuget;
      }

      public boolean getExcludeVersion() {
        return getBoolean(context, NUGET_EXCLUDE_VERSION);
      }

      public boolean getNoCache(){
        return getBoolean(context, NUGET_NO_CACHE);
      }

      @NotNull
      public PackagesInstallMode getInstallMode() throws RunBuildException {
        String useRestoreParamValue = getParameter(context, NUGET_USE_RESTORE_COMMAND);
        final PackagesInstallMode mode = PackagesInstallMode.parse(useRestoreParamValue);
        return mode != null ? mode : PackagesInstallMode.VIA_RESTORE;
      }
    };
  }

  public PackagesUpdateParameters loadUpdatePackagesParameters(@NotNull final BuildRunnerContext context,
                                                               @NotNull final NuGetFetchParameters nuget) throws RunBuildException {
    if (!getBoolean(context, NUGET_UPDATE_PACKAGES)) return null;

    return new PackagesUpdateParameters() {
      @NotNull
      public NuGetFetchParameters getNuGetParameters() {
        return nuget;
      }

      @NotNull
      public PackagesUpdateMode getUpdateMode() {
        PackagesUpdateMode mode = PackagesUpdateMode.parse(getParameter(context, NUGET_UPDATE_MODE));
        return mode == null ? PackagesUpdateMode.FOR_SLN : mode;
      }

      public boolean getUseSafeUpdate() {
        return getBoolean(context, NUGET_UPDATE_PACKAGES_SAFE);
      }

      public boolean getIncludePrereleasePackages() {
        return getBoolean(context, NUGET_UPDATE_PACKAGES_PRERELEASE);
      }

      @NotNull
      public Collection<String> getPackagesToUpdate() {
        return getMultilineParameter(context, NUGET_UPDATE_PACKAGE_IDS);
      }

      @NotNull
      public Collection<String> getCustomCommandline() {
        return getMultilineParameter(context, NUGET_UPDATE_CUSOM_COMMANDLINE, true);
      }
    };
  }

  @NotNull
  public NuGetPublishParameters loadPublishParameters(@NotNull final BuildRunnerContext context) throws RunBuildException {
    return new NuGetPublishParameters() {
      public String getPublishSource() throws RunBuildException {
        return getParameter(context, NUGET_PUBLISH_SOURCE);
      }

      @Nullable
      public String getApiKey() throws RunBuildException {
        return getParameter(context, NUGET_API_KEY);
      }

      @NotNull
      public Collection<String> getFiles() throws RunBuildException {
        return getMultilineParameter(context, NUGET_PUBLISH_FILES);
      }

      @NotNull
      public Collection<String> getCustomCommandline() {
        return getMultilineParameter(context, NUGET_PUSH_CUSTOM_COMMANDLINE, true);
      }

      @NotNull
      public File getNuGetExeFile() throws RunBuildException {
        return getPathToNuGet(context);
      }
    };
  }

  @NotNull
  public NuGetPackParameters loadPackParameters(@NotNull final BuildRunnerContext context) throws RunBuildException {
    return new NuGetPackParameters() {
      @NotNull
      public Collection<String> getSpecFiles() throws RunBuildException {
        return getMultilineParameter(context, NUGET_PACK_SPEC_FILE);
      }

      @NotNull
      public Collection<String> getExclude() {
        return getMultilineParameter(context, NUGET_PACK_EXCLUDE_FILES);
      }

      @NotNull
      public Collection<String> getProperties() {
        return getMultilineParameter(context, NUGET_PACK_PROPERTIES);
      }

      @NotNull
      public Collection<String> getCustomCommandline() {
        return getMultilineParameter(context, NUGET_PACK_CUSOM_COMMANDLINE, true);
      }

      @NotNull
      public File getOutputDirectory() throws RunBuildException {
        return getDirectory(context, NUGET_PACK_OUTPUT_DIR, "output directory");
      }

      public boolean cleanOutputDirectory() throws RunBuildException {
        return getBoolean(context, NUGET_PACK_OUTPUT_CLEAR);
      }

      @NotNull
      public PackagesPackDirectoryMode getBaseDirectoryMode() {
        return PackagesPackDirectoryMode.fromString(getParameter(context, NUGET_PACK_BASE_DIRECTORY_MODE));
      }

      @NotNull
      public File getBaseDirectory() throws RunBuildException {
        String path = getParameter(context, NUGET_PACK_BASE_DIR);
        if (path == null || StringUtil.isEmptyOrSpaces(path)) {
          return context.getBuild().getCheckoutDirectory();
        }

        final File file = FileUtil.resolvePath(context.getBuild().getCheckoutDirectory(), path);
        if (!file.isDirectory()) {
          //noinspection ResultOfMethodCallIgnored
          file.mkdirs();
        }

        if (!file.isDirectory()) {
          throw new RunBuildException("Failed to find or create base directory at " + file);
        }

        return file;
      }

      @Nullable
      public String getVersion() throws RunBuildException {
        return getParameter(context, NUGET_PACK_VERSION);
      }

      public boolean packSymbols() {
        return getBoolean(context, NUGET_PACK_INCLUDE_SOURCES);
      }

      public boolean packTool() {
        return getBoolean(context, NUGET_PACK_AS_TOOL);
      }

      public boolean publishAsArtifacts() {
        return getBoolean(context, NUGET_PACK_PUBLISH_ARTIFACT);
      }

      public boolean preferProjectFileToNuSpec() {
        return getBoolean(context, NUGET_PACK_PREFER_PROJECT);
      }

      @NotNull
      public File getNuGetExeFile() throws RunBuildException {
        return getPathToNuGet(context);
      }
    };
  }
}
