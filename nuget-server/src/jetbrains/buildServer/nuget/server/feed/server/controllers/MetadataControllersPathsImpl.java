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

package jetbrains.buildServer.nuget.server.feed.server.controllers;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 24.10.11 19:47
 */
public class MetadataControllersPathsImpl implements MetadataControllersPaths {
  private final PluginDescriptor myDescriptor;

  public MetadataControllersPathsImpl(@NotNull final PluginDescriptor descriptor) {
    myDescriptor = descriptor;
  }

  @NotNull
  public String getBasePath() {
    return myDescriptor.getPluginResourcesPath();
  }

  @NotNull
  public String getMetadataControllerPath() {
    return myDescriptor.getPluginResourcesPath("packages-metadata.html");
  }

  @NotNull
  public String getPingControllerPath() {
    return myDescriptor.getPluginResourcesPath("packages-ping.html");
  }
}
