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

import jetbrains.buildServer.nuget.server.feed.server.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.PackagesIndex;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 21.10.11 16:58
 */
public class PackageWriterImpl implements PackagesWriter {
  @NotNull
  private final PackagesIndex myIndex;
  @NotNull
  private final PackageInfoSerializer mySerializer;

  public PackageWriterImpl(@NotNull final PackagesIndex index,
                           @NotNull final PackageInfoSerializer serializer) {
    myIndex = index;
    mySerializer = serializer;
  }

  public void serializePackages(@NotNull final HttpServletRequest request,
                                @NotNull final HttpServletResponse response) throws IOException {
    final PrintWriter writer = response.getWriter();
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();
    while (it.hasNext()) {
      final NuGetIndexEntry e = it.next();
      mySerializer.serializePackage(
                        e.getAttributes(),
                        e.getBuildTypeId(),
                        e.getBuildId(),
                        e.isIsLatestVersion(),
                        writer
                );
                writer.write("\r\n");
    }

    writer.flush();
  }
}
