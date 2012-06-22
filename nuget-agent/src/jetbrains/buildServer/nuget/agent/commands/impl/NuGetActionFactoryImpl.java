/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.CommandFactory;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.DelegatingBuildProcess;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 17:49
 */
public class NuGetActionFactoryImpl implements NuGetActionFactory {
  private static final Logger LOG = Logger.getInstance(NuGetActionFactoryImpl.class.getName());

  private final CommandFactory myCommandFactory;
  private final NuGetProcessCallback myFactory;
  private final PackageUsages myPackageUsages;

  public NuGetActionFactoryImpl(@NotNull final NuGetProcessCallback factory,
                                @NotNull final PackageUsages packageUsages,
                                @NotNull final CommandFactory commandFactory) {
    myFactory = factory;
    myPackageUsages = packageUsages;
    myCommandFactory = commandFactory;
  }

  @NotNull
  protected CommandFactory.Callback<BuildProcess> getCallback(@NotNull final BuildRunnerContext context) {
    return myFactory.getCallback(context);
  }

  @NotNull
  public BuildProcess createInstall(@NotNull final BuildRunnerContext context,
                                    @NotNull final PackagesInstallParameters params,
                                    @NotNull final File packagesConfig,
                                    @NotNull final File targetFolder) throws RunBuildException {
    return myCommandFactory.createInstall(params, packagesConfig, targetFolder, getCallback(context));
  }


  @NotNull
  public BuildProcess createUpdate(@NotNull final BuildRunnerContext context,
                                   @NotNull final PackagesUpdateParameters params,
                                   @NotNull final File packagesConfig,
                                   @NotNull final File targetFolder) throws RunBuildException {
    return myCommandFactory.createUpdate(params, packagesConfig, targetFolder, getCallback(context));
  }

  @NotNull
  public BuildProcess createAuthenticateFeeds(@NotNull final BuildRunnerContext context,
                                              @NotNull final Collection<PackageSource> sources,
                                              @NotNull final NuGetParameters params) throws RunBuildException {

    return new DelegatingBuildProcess(new DelegatingBuildProcess.Action() {
      private File myTempFile;
      @NotNull
      public BuildProcess startImpl() throws RunBuildException {
        final Element root = new Element("teamcity-feeds-list") {{
          addContent((Content)new Element("feeds") {{
            for (final PackageSource source : sources) {
              addContent((Content)new Element("feed"){{
                setAttribute("url", source.getSource());
                final String userName = source.getUserName();
                final String password = source.getPassword();
                if (!StringUtil.isEmptyOrSpaces(userName) && !StringUtil.isEmptyOrSpaces(password)) {
                  setAttribute("user", userName);
                  setAttribute("password", password);
                }
              }});
            }
          }});
        }};

        try {
          myTempFile = FileUtil.createTempFile(context.getBuild().getAgentTempDirectory(), "nuget", "teamcity", true);
        } catch (IOException e) {
          LOG.warn("Failed to create temp file. " + e.getMessage(), e);
          throw new RunBuildException("Failed to create temp file. " + e.getMessage());
        }

        try {
          FileUtil.saveDocument(new Document(root), myTempFile);
        } catch (IOException e) {
          LOG.warn("Failed to save command parameters to " + root + ". " + e.getMessage(), e);
          throw new RunBuildException("Failed to save command parameters to " + root + ". " + e.getMessage());
        }

        return myCommandFactory.createAuthorizeFeed(
                myTempFile,
                params,
                context.getWorkingDirectory(),
                getCallback(context)
                );
      }

      public void finishedImpl() {
        if (myTempFile != null) {
          FileUtil.delete(myTempFile);
        }
      }
    });
  }

  @NotNull
  public BuildProcess createVersionCheckCommand(@NotNull final BuildRunnerContext context,
                                                @NotNull final File versionFile,
                                                @NotNull final NuGetParameters params) throws RunBuildException {
    return  new DelegatingBuildProcess(new DelegatingBuildProcess.Action() {
      @NotNull
      public BuildProcess startImpl() throws RunBuildException {
        FileUtil.delete(versionFile);
        return myCommandFactory.createVersionCheck(params, versionFile, context.getWorkingDirectory(), getCallback(context));
      }

      public void finishedImpl() {
      }
    });
  }

  @NotNull
  public BuildProcess createDeAuthenticateFeeds(@NotNull BuildRunnerContext context, @NotNull NuGetParameters params) throws RunBuildException {
    return myCommandFactory.createDeAuthorizeFeed(params, context.getWorkingDirectory(), getCallback(context));
  }

  @NotNull
  public BuildProcess createUsageReport(@NotNull final BuildRunnerContext context,
                                        @NotNull final File packagesConfig,
                                        @NotNull final File targetFolder) throws RunBuildException {
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
  public BuildProcess createPush(@NotNull BuildRunnerContext context,
                                 @NotNull NuGetPublishParameters params,
                                 @NotNull File packagePath) throws RunBuildException {
    final PackageSource source = params.getPublishSource();
    final Collection<PackageSource> sources = source == null
            ? Collections.<PackageSource>emptyList()
            : Collections.singleton(source);

    return myCommandFactory.createPush(params, packagePath, getCallback(context));
  }

  @NotNull
  public BuildProcess createPack(@NotNull BuildRunnerContext context,
                                 @NotNull File specFile,
                                 @NotNull NuGetPackParameters params) throws RunBuildException {
    return myCommandFactory.createPack(
            specFile,
            params,
            context.getBuild().getCheckoutDirectory(),
            getCallback(context));
  }
}
