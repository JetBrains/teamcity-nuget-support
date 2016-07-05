/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedCredentials;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.tool.NuGetServerToolProvider;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequestFactory;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestModeFactory;
import jetbrains.buildServer.tools.ServerToolManager;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import static jetbrains.buildServer.nuget.common.FeedConstants.PATH_TO_NUGET_EXE;
import static jetbrains.buildServer.nuget.server.trigger.TriggerConstants.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 10.05.12 13:25
 */
public class TriggerRequestFactory {
  private final CheckRequestModeFactory myModeFactory;
  private final ServerToolManager myToolManager;
  private final PackageCheckRequestFactory myRequestFactory;
  private final Collection<TriggerUrlPostProcessor> myUrlPostProcessors;

  public TriggerRequestFactory(@NotNull final CheckRequestModeFactory modeFactory,
                               @NotNull final ServerToolManager toolManager,
                               @NotNull final PackageCheckRequestFactory requestFactory,
                               @NotNull final Collection<TriggerUrlPostProcessor> urlPostProcessors) {
    myModeFactory = modeFactory;
    myToolManager = toolManager;
    myRequestFactory = requestFactory;
    myUrlPostProcessors = urlPostProcessors;
  }

  @NotNull
  public PackageCheckRequest createRequest(@NotNull PolledTriggerContext context) throws BuildTriggerException {
    final BuildTriggerDescriptor descriptor = context.getTriggerDescriptor();
    final Map<String, String> descriptorProperties = descriptor.getProperties();
    final String pkgId = descriptorProperties.get(PACKAGE);
    final String version = descriptorProperties.get(VERSION);
    final String username = descriptorProperties.get(USERNAME);
    final String password = descriptorProperties.get(PASSWORD);
    boolean isPrerelease = !StringUtil.isEmptyOrSpaces(descriptorProperties.get(INCLUDE_PRERELEASE));

    NuGetFeedCredentials credentials = null;
    if (username != null && password != null && !StringUtil.isEmptyOrSpaces(username) && !StringUtil.isEmptyOrSpaces(password)) {
      credentials = new NuGetFeedCredentials(username, password);
    }

    if (StringUtil.isEmptyOrSpaces(pkgId)) {
      throw new BuildTriggerException("The Package Id must be specified");
    }

    final String nugetVersionRef = descriptorProperties.get(FeedConstants.NUGET_PATH_TRIGGER_PARAM_NAME);
    if(nugetVersionRef == null) {
      throw new BuildTriggerException("Trigger descriptor doesn't provide path to nuget.exe via parameter " + FeedConstants.NUGET_PATH_TRIGGER_PARAM_NAME);
    }
    final File nugetToolRootPath = myToolManager.getUnpackedToolVersionPath(NuGetServerToolProvider.NUGET_TOOL_TYPE, nugetVersionRef, context.getBuildType().getProject());
    if(nugetToolRootPath == null) {
      throw new BuildTriggerException("Failed to find NuGet.exe by tool reference: " + nugetVersionRef);
    }
    final File nugetPath = new File(nugetToolRootPath, PATH_TO_NUGET_EXE);
    if (!nugetPath.isFile()) {
      throw new BuildTriggerException("Failed to find NuGet.exe at: " + nugetPath);
    }

    String source = descriptorProperties.get(SOURCE);
    if(StringUtils.isEmpty(source)){
      source = null;
    }
    else {
      for (TriggerUrlPostProcessor urlPostProcessor : myUrlPostProcessors) {
        source = urlPostProcessor.updateTriggerUrl(descriptor, source);
      }
    }

    return myRequestFactory.createRequest(
            myModeFactory.createNuGetChecker(nugetPath),
            new SourcePackageReference(source, credentials, pkgId, version, isPrerelease));
  }
}
