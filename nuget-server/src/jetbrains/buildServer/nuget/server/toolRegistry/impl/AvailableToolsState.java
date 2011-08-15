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

package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.nuget.server.feed.reader.FeedConstants;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetTool;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 15.08.11 16:33
 */
public class AvailableToolsState {
  private final NuGetFeedReader myReader;

  public AvailableToolsState(@NotNull final NuGetFeedReader reader) {
    myReader = reader;
  }

  public Collection<NuGetTool> getAvailable() {
    try {
      final Collection<FeedPackage> packages = myReader.queryPackageVersions(FeedConstants.FEED_URL, FeedConstants.NUGET_COMMANDLINE);
      return CollectionsUtil.convertCollection(
              packages,
              new Converter<NuGetTool, FeedPackage>() {
                public NuGetTool createFrom(@NotNull FeedPackage source) {
                  return new InstallableTool(source);
                }
              }
              );

    } catch (IOException e) {
      e.printStackTrace();
      //TODO: handle exception
      return Collections.emptyList();
    }
  }

  private static class InstallableTool implements NuGetTool {
    private final FeedPackage myPackage;

    private InstallableTool(@NotNull final FeedPackage aPackage) {
      myPackage = aPackage;
    }

    @NotNull
    public String getId() {
      return myPackage.getAtomId();
    }

    @NotNull
    public String getVersion() {
      return myPackage.getInfo().getVersion();
    }

    @NotNull
    public FeedPackage getPackage() {
      return myPackage;
    }
  }
}
