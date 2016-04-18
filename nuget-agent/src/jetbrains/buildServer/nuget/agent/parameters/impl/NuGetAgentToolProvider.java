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
public class NuGetAgentToolProvider {

  private static final Logger LOG = Logger.getInstance(NuGetAgentToolProvider.class.getName());

  public NuGetAgentToolProvider(@NotNull final ToolProvidersRegistry toolProvidersRegistry,
                                @NotNull final BundledToolsRegistry bundledTools) {
    toolProvidersRegistry.registerToolProvider(new ToolProvider() {
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
            cltPath = buildRunnerContext.getBuildParameters().getAllParameters().get(defaultToolVersionParameterName);
            LOG.debug(String.format("Resolved default %s tool version to %s. Will use it further.",ToolConstants.NUGET_TOOL_TYPE_ID, cltPath));
          }
          final String bundledCltVersion = ToolVersionReference.getToolVersionOfType(ToolConstants.NUGET_TOOL_TYPE_ID, cltPath);
          if(bundledCltVersion != null){
            final BundledTool bundledTool = bundledTools.findTool(ToolIdUtils.getIdFromVersion(bundledCltVersion));
            if(bundledTool != null){
              final File bundledToolRootPath = bundledTool.getRootPath();
              if (bundledToolRootPath.isDirectory()) {
                return bundledToolRootPath.getAbsolutePath();
              }
            }
          }
        }
        throw new ToolCannotBeFoundException("NuGet.exe can't be located for build " + LogUtil.describe(buildRunnerContext.getBuild()));
      }
    });
  }
}
