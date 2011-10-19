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
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.nuget.server.feed.server.PackagesIndex;
import jetbrains.buildServer.serverSide.metadata.ArtifactsMetadataEntry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

import static jetbrains.buildServer.nuget.server.feed.server.PackagesIndex.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 16:05
 */
public class MetadataController extends BaseController {
  private final String myPath;
  private final PackagesIndex myStorage;

  public MetadataController(@NotNull final WebControllerManager web,
                            @NotNull final PluginDescriptor descriptor,
                            @NotNull final PackagesIndex storage) {
    myStorage = storage;
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

      Map<String, String> parameters = new TreeMap<String, String>(COMPARER);

      parameters.putAll(e.getMetadata());

      final String buildTypeId = parameters.get(TEAMCITY_BUILD_TYPE_ID);
      final String relPath = parameters.get(TEAMCITY_ARTIFACT_RELPATH);
      parameters.put(TEAMCITY_BUILD_ID, String.valueOf(e.getBuildId()));
      parameters.put(TEAMCITY_DOWNLOAD_URL, "/repository/download/" + buildTypeId + "/" + e.getBuildId() + ":id/" + relPath);

      writer.write(ServiceMessage.asString("package", parameters));
      writer.write("\r\n");
    }
    writer.flush();
    return null;
  }

  private static final Comparator<String> COMPARER = new Comparator<String>() {
    @NotNull
    private Integer power(@NotNull String key) {
      if ("Id".equals(key)) return 5;
      if ("Version".equals(key)) return 4;
      if (key.startsWith("teamcity")) return 3;
      return 0;
    }

    public int compare(@NotNull String o1, @NotNull String o2) {
      final Integer p1 = power(o1);
      final Integer p2 = power(o2);
      if (p1 > p2) return -1;
      if (p1 < p2) return 1;
      return o1.compareTo(o2);
    }
  };
}
