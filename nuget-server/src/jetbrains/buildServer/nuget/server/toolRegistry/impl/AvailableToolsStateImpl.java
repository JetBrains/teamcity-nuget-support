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

import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.NuGetFeedReader;
import jetbrains.buildServer.nuget.server.toolRegistry.FetchException;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetTool;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolsPolicy;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.TimeService;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 15.08.11 16:33
 */
public class AvailableToolsStateImpl implements AvailableToolsState {
  private static final long TIMEOUT = 1000 * 60 * 15; //15 min

  private final NuGetFeedReader myReader;
  private final TimeService myTime;
  private Collection<InstallableTool> myTools;
  private long lastRequest = 0;

  public AvailableToolsStateImpl(@NotNull final NuGetFeedReader reader,
                                 @NotNull final TimeService time) {
    myReader = reader;
    myTime = time;
  }

  @Nullable
  public FeedPackage findTool(@NotNull final String id) {
    final Collection<InstallableTool> tools = myTools;
    if (tools != null) {
      for (InstallableTool tool : tools) {
        if(tool.getPackage().getAtomId().equals(id)) {
          return tool.getPackage();
        }
      }
    }
    return null;
  }

  @NotNull
  public Collection<? extends NuGetTool> getAvailable(ToolsPolicy policy) throws FetchException {
    Collection<InstallableTool> nuGetTools = myTools;
    if (policy == ToolsPolicy.FetchNew
            || nuGetTools == null
            || lastRequest + TIMEOUT < myTime.now()) {
      myTools = null;
      myTools = nuGetTools = fetchAvailable();
      lastRequest = myTime.now();
    }
    return nuGetTools;
  }

  private Collection<InstallableTool> fetchAvailable() throws FetchException {
    try {
      final Collection<FeedPackage> packages = myReader.queryPackageVersions(FeedConstants.FEED_URL, FeedConstants.NUGET_COMMANDLINE);
      return CollectionsUtil.filterAndConvertCollection(
              packages,
              new Converter<InstallableTool, FeedPackage>() {
                public InstallableTool createFrom(@NotNull FeedPackage source) {
                  return new InstallableTool(source);
                }
              },
              new Filter<FeedPackage>() {
                public boolean accept(@NotNull FeedPackage data) {
                  final String[] version = data.getInfo().getVersion().split("\\.");
                  if (version.length < 2) return false;
                  int major = parse(version[0]);
                  if (major < 1) return false;

                  int minor = parse(version[1]);
                  if (minor < 4) return false;

                  return true;
                }

                private int parse(String s) {
                  try {
                    return Integer.parseInt(s.trim());
                  } catch (Exception e) {
                    return -1;
                  }
                }
              }
      );

    } catch (IOException e) {
      throw new FetchException(e.getMessage(), e);
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
