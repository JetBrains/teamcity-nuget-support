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

import jetbrains.buildServer.nuget.server.toolRegistry.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 1:07
 */
public class NuGetToolManagerImpl implements NuGetToolManager {
  private final AvailableToolsState myAvailables;

  public NuGetToolManagerImpl(AvailableToolsState availables) {
    myAvailables = availables;
  }

  @NotNull
  public Collection<NuGetInstalledTool> getInstalledTools() {
    return mockInstalledTools();
  }

  @NotNull
  public Collection<NuGetInstallingTool> getInstallingTool() {
    return mockInstallingTools();
  }


  @NotNull
  public Collection<NuGetTool> getAvailableTools() {
    //This must be cached to make if work faster!
    return myAvailables.getAvailable();
  }


  public void installTool(@NotNull NuGetTool tool, @NotNull ActionProgress progress) {

  }

  private List<NuGetInstallingTool> mockInstallingTools() {
    return Arrays.<NuGetInstallingTool>asList(
            new NuGetInstallingTool() {
              @NotNull
              public Collection<String> getInstallMessages() {
                return Arrays.asList("mgs1", "msg2", "msg3");
              }

              @NotNull
              public String getId() {
                return "ii-1";
              }

              @NotNull
              public String getVersion() {
                return "ii.1.2.43";
              }
            }
    );
  }

  private List<NuGetInstalledTool> mockInstalledTools() {
    return Arrays.<NuGetInstalledTool>asList(
            new NuGetInstalledTool() {
              @NotNull
              public File getPath() {
                return new File(".");
              }

              @NotNull
              public String getId() {
                return "i1";
              }

              @NotNull
              public String getVersion() {
                return "i1.2.3.5";
              }
            },
            new NuGetInstalledTool() {
              @NotNull
              public File getPath() {
                return new File(".");
              }

              @NotNull
              public String getId() {
                return "i2";
              }

              @NotNull
              public String getVersion() {
                return "i2.4.5.7";
              }
            }
    );
  }
}
