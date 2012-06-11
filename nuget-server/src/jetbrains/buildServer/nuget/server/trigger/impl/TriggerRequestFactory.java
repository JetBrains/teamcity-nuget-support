/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.trigger.impl;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolManager;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static jetbrains.buildServer.nuget.server.trigger.TriggerConstants.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 10.05.12 13:25
 */
public class TriggerRequestFactory {
  private final CheckRequestModeFactory myModeFactory;
  private final NuGetToolManager myManager;
  private final PackageCheckRequestFactory myRequestFactory;
  private final RootUrlHolder myHolder;

  public TriggerRequestFactory(@NotNull final CheckRequestModeFactory modeFactory,
                               @NotNull final NuGetToolManager manager,
                               @NotNull final PackageCheckRequestFactory requestFactory,
                               @NotNull final RootUrlHolder holder) {
    myModeFactory = modeFactory;
    myManager = manager;
    myRequestFactory = requestFactory;
    myHolder = holder;
  }

  @NotNull
  public PackageCheckRequest createRequest(@NotNull BuildTriggerDescriptor descriptor) throws BuildTriggerException {
    String path = myManager.getNuGetPath(descriptor.getProperties().get(NUGET_EXE));
    String pkgId = descriptor.getProperties().get(PACKAGE);
    String version = descriptor.getProperties().get(VERSION);
    String source = descriptor.getProperties().get(SOURCE);
    boolean isPrerelease = !StringUtil.isEmptyOrSpaces(descriptor.getProperties().get(INCLUDE_PRERELEASE));

    if (StringUtil.isEmptyOrSpaces(path)) {
      throw new BuildTriggerException("Path to NuGet.exe must be specified");
    }

    if (StringUtil.isEmptyOrSpaces(pkgId)) {
      throw new BuildTriggerException("Package Id must be specified");
    }

    final File nugetPath = new File(path);
    if (!nugetPath.isFile()) {
      throw new BuildTriggerException("Failed to find NuGet.exe at: " + nugetPath);
    }

    if (source != null && ReferencesResolverUtil.mayContainReference(source)) {
      final String rootUrlPattern = "%" + AgentRuntimeProperties.TEAMCITY_SERVER_URL + "%";
      final String rootUrlValue = myHolder.getRootUrl();

      source = source.replace(rootUrlPattern, rootUrlValue);
    }

    return myRequestFactory.createRequest(
            myModeFactory.createNuGetChecker(nugetPath),
            new SourcePackageReference(source, pkgId, version, isPrerelease));
  }
}
