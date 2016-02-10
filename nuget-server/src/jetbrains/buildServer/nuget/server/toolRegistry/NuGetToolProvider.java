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

package jetbrains.buildServer.nuget.server.toolRegistry;

import jetbrains.buildServer.nuget.server.toolRegistry.impl.InstalledTool;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolsRegistry;
import jetbrains.buildServer.tools.ToolProvider;
import jetbrains.buildServer.tools.ToolType;
import jetbrains.buildServer.tools.ToolTypeExtension;
import jetbrains.buildServer.tools.ToolVersion;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by Evgeniy.Koshkin on 15-Jan-16.
 */
public class NuGetToolProvider implements ToolProvider {

  private static final ToolTypeExtension NUGET_TOOL_TYPE = new ToolTypeExtension() {
    @NotNull
    public String getType() {
      return ToolConstants.NUGET_TOOL_TYPE_ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
      return "NuGet.exe";
    }

    @Nullable
    @Override
    public String getDescription() {
      return "Installed NuGet versions are automatically distributed to all build agents and can be used in NuGet-related runners.";
    }
  };
  @NotNull
  private final ToolsRegistry myToolsRegistry;

  public NuGetToolProvider(@NotNull ToolsRegistry toolsRegistry) {
    myToolsRegistry = toolsRegistry;
  }

  @NotNull
  public ToolType getType() {
    return NUGET_TOOL_TYPE;
  }

  @NotNull
  public Collection<ToolVersion> getInstalledToolVersions() {
    return CollectionsUtil.convertCollection(myToolsRegistry.getTools(), new Converter<ToolVersion, InstalledTool>() {
      public ToolVersion createFrom(@NotNull final InstalledTool source) {
        return new ToolVersion(NUGET_TOOL_TYPE, source.getVersion());
      }
    });
  }
}
