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

package jetbrains.buildServer.nuget.agent.commands.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.parameters.PackageSourceManager;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import jetbrains.buildServer.nuget.common.auth.PackageSourceUtil;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static jetbrains.buildServer.nuget.common.auth.NuGetAuthConstants.NUGET_CREDENTIALPROVIDERS_PATH_ENV_VAR;
import static jetbrains.buildServer.nuget.common.auth.NuGetAuthConstants.TEAMCITY_NUGET_FEEDS_ENV_VAR;

/**
 * Created 04.01.13 19:26
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetAuthCommandBuildProcessFactory implements CommandlineBuildProcessFactory {

  private static final Logger LOG = Logger.getInstance(NuGetAuthCommandBuildProcessFactory.class.getName());

  private final CommandlineBuildProcessFactory myFactory;
  private final NuGetTeamCityProvider myProvider;
  private final PackageSourceManager mySources;

  public NuGetAuthCommandBuildProcessFactory(@NotNull final CommandlineBuildProcessFactory factory,
                                             @NotNull final NuGetTeamCityProvider provider,
                                             @NotNull final PackageSourceManager sources) {
    myFactory = factory;
    myProvider = provider;
    mySources = sources;
  }

  @NotNull
  public BuildProcess executeCommandLine(@NotNull final BuildRunnerContext hostContext,
                                         @NotNull final String program,
                                         @NotNull final Collection<String> _argz,
                                         @NotNull final File workingDir,
                                         @NotNull final Map<String, String> _additionalEnvironment) throws RunBuildException {

    final Collection<PackageSource> sources = getSecureSources(hostContext);

    if(LOG.isDebugEnabled()){
      LOG.debug("Provided credentials for NuGet packages sources: " + StringUtil.join(", ", CollectionsUtil.convertAndFilterNulls(sources, new Converter<String, PackageSource>() {
        public String createFrom(@NotNull PackageSource source) {
          return source.getSource();
        }
      })));
    }

    final Map<String, String> additionalEnvironment = new HashMap<String, String>(_additionalEnvironment);
    final String credentialsProviderPath = myProvider.getCredentialProviderHomeDirectory().getAbsolutePath();
    LOG.debug("Set credentials provider location to " + credentialsProviderPath);
    additionalEnvironment.put(NUGET_CREDENTIALPROVIDERS_PATH_ENV_VAR, credentialsProviderPath);

    if (sources.isEmpty() || hostContext.getConfigParameters().containsKey("teamcity.nuget.disableTeamCityRunner"))  {
      return myFactory.executeCommandLine(
              hostContext,
              program,
              _argz,
              workingDir,
              additionalEnvironment
      );
    }

    return new DelegatingBuildProcess(new DelegatingBuildProcess.Action() {
      private File mySourcesFile;
      @NotNull
      public BuildProcess startImpl() throws RunBuildException {
        final String executable = myProvider.getNuGetRunnerPath().getAbsolutePath();
        final List<String> argz = new ArrayList<String>(_argz);
        argz.add(0, program);

        try {
          mySourcesFile = FileUtil.createTempFile(hostContext.getBuild().getAgentTempDirectory(), "nuget-sources", ".xml", true);
          PackageSourceUtil.writeSources(mySourcesFile, sources);
        } catch (IOException e) {
          throw new RunBuildException("Failed to create temp file for NuGet sources. " + e.getMessage(), e);
        }

        additionalEnvironment.put(TEAMCITY_NUGET_FEEDS_ENV_VAR, mySourcesFile.getPath());

        return myFactory.executeCommandLine(
                hostContext,
                executable,
                argz,
                workingDir,
                additionalEnvironment
        );
      }

      public void finishedImpl() {
        if (mySourcesFile != null) {
          FileUtil.delete(mySourcesFile);
        }
      }
    });
  }

  @NotNull
  private Collection<PackageSource> getSecureSources(BuildRunnerContext hostContext) {
    List<PackageSource> sources = new ArrayList<PackageSource>();
    for (PackageSource source : mySources.getGlobalPackageSources(hostContext.getBuild())) {
      if (source.getUsername() != null && source.getPassword() != null) {
        sources.add(source);
      }
    }
    return sources;
  }
}
