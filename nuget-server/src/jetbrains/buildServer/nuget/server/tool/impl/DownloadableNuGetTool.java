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

package jetbrains.buildServer.nuget.server.tool.impl;

import jetbrains.buildServer.nuget.server.tool.NuGetServerToolProvider;
import jetbrains.buildServer.tools.ToolType;
import jetbrains.buildServer.tools.available.DownloadableToolVersion;
import org.jetbrains.annotations.NotNull;

/**
 * @author Evgeniy.Koshkin
 */
public class DownloadableNuGetTool implements DownloadableToolVersion {
  private final String myId;
  @NotNull
  private final String myVersion;
  @NotNull
  private final String myDownloadUrl;
  @NotNull
  private final String myDestinationFileName;

  public DownloadableNuGetTool(@NotNull String version, @NotNull String downloadUrl, @NotNull String destinationFileName) {
    myVersion = version;
    myDownloadUrl = downloadUrl;
    myDestinationFileName = destinationFileName;
    myId = NuGetServerToolProvider.NUGET_TOOL_TYPE.getType() + "." + version;
  }

  @NotNull
  public String getId(){
    return myId;
  }

  @NotNull
  @Override
  public String getDownloadUrl() {
    return myDownloadUrl;
  }

  @NotNull
  @Override
  public String getDestinationFileName() {
    return myDestinationFileName;
  }

  @NotNull
  @Override
  public ToolType getType() {
    return NuGetServerToolProvider.NUGET_TOOL_TYPE;
  }

  @NotNull
  @Override
  public String getVersion() {
    return myVersion;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return myId;
  }
}
