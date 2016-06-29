/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.log.LogUtil;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.ToolConstants;
import jetbrains.buildServer.nuget.common.ToolIdUtils;
import jetbrains.buildServer.tools.DefaultToolVersionParameters;
import jetbrains.buildServer.tools.ToolVersionReference;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_PATH;

/**
 * Created by Evgeniy.Koshkin.
 */
public class NuGetAgentToolProvider implements ToolProvider {
  private static final Logger LOG = Logger.getInstance(NuGetAgentToolProvider.class.getName());

  @NotNull private final BundledToolsRegistry myBundledTools;

  public NuGetAgentToolProvider(@NotNull final BundledToolsRegistry bundledTools) {
    myBundledTools = bundledTools;
  }

  @Override
  public boolean supports(@NotNull final String toolName) {
    return toolName.equalsIgnoreCase(ToolConstants.NUGET_TOOL_TYPE_ID);
  }

  @NotNull
  @Override
  public String getPath(@NotNull final String toolName) throws ToolCannotBeFoundException {
    throw new ToolCannotBeFoundException("NuGet.exe can be located in scope of particular build only.");
  }

  @NotNull
  @Override
  public String getPath(@NotNull final String toolName, @NotNull AgentRunningBuild agentRunningBuild, @NotNull BuildRunnerContext buildRunnerContext) throws ToolCannotBeFoundException {
    final Map<String, String> runParameters = buildRunnerContext.getRunnerParameters();
    String cltPath = runParameters.get(NUGET_PATH);
    if(!StringUtil.isEmpty(cltPath)){
      if (!ToolVersionReference.isToolReference(cltPath)) {
        LOG.debug("Located R# CLT via path " + cltPath);
        return cltPath;
      }
      if(ToolVersionReference.isDefaultVersionReference(cltPath)){
        final String defaultToolVersionParameterName = DefaultToolVersionParameters.getParameterName(ToolConstants.NUGET_TOOL_TYPE_ID);
        cltPath = agentRunningBuild.getSharedConfigParameters().get(defaultToolVersionParameterName);
        if(cltPath == null)
          throw new ToolCannotBeFoundException("Default version of NuGet.exe can't be found in running build parameters by name " + defaultToolVersionParameterName);

        LOG.debug(String.format("Resolved default %s tool version to %s. Will use it further.",ToolConstants.NUGET_TOOL_TYPE_ID, cltPath));
      }
      final String bundledNugetVersion = ToolVersionReference.getToolVersionOfType(ToolConstants.NUGET_TOOL_TYPE_ID, cltPath);
      if(bundledNugetVersion != null){
        final String nugetToolName = ToolIdUtils.getIdFromVersion(bundledNugetVersion);
        final BundledTool bundledTool = myBundledTools.findTool(nugetToolName);
        if (bundledTool == null)
          throw new ToolCannotBeFoundException("NuGet.exe can't be found in registered bundled tools by name " + nugetToolName);

        final File bundledToolRootPath = bundledTool.getRootPath();
        if (!bundledToolRootPath.isDirectory())
          throw new ToolCannotBeFoundException("Found NuGet.exe home directory doesn't exist on path " + bundledToolRootPath.getAbsolutePath());

        return new File(bundledToolRootPath, FeedConstants.PATH_TO_NUGET_EXE).getAbsolutePath();
      }
    }
    throw new ToolCannotBeFoundException("NuGet.exe can't be located for build " + LogUtil.describe(buildRunnerContext.getBuild()));
  }
}
