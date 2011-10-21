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

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.nuget.server.feed.server.PackagesIndex;
import jetbrains.buildServer.serverSide.metadata.ArtifactsMetadataEntry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 16:05
 */
public class MetadataController extends BaseController {
  @NotNull private final String myPath;
  @NotNull private final PackagesIndex myStorage;
  @NotNull private final PackagesWriter myWriter;

  public MetadataController(@NotNull final WebControllerManager web,
                            @NotNull final PluginDescriptor descriptor,
                            @NotNull final PackagesIndex storage,
                            @NotNull PackagesWriter writer) {
    myStorage = storage;
    myWriter = writer;
    myPath = descriptor.getPluginResourcesPath("packages-metadata.html");
    web.registerController(myPath, this);
  }

  @NotNull
  public String getPath() {
    return myPath;
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setCharacterEncoding("utf-8");
    response.setContentType("text/plain");

    final PrintWriter writer = response.getWriter();
    final Iterator<ArtifactsMetadataEntry> entries = myStorage.getEntries();
    final Set<String> reportedPackages = new HashSet<String>();

    while (entries.hasNext()) {
      final ArtifactsMetadataEntry e = entries.next();
      //remove duplicates
      if (!reportedPackages.add(e.getKey())) continue;

      myWriter.serializePackage(e, writer);

      writer.write("\r\n");
    }
    writer.flush();
    return null;
  }
}
