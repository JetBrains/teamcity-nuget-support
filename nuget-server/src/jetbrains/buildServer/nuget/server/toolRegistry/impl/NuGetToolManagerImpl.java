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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.toolRegistry.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 1:07
 */
public class NuGetToolManagerImpl implements NuGetToolManager {
  private static final Logger LOG = Logger.getInstance(NuGetToolManagerImpl.class.getName());

  private final AvailableToolsState myAvailables;
  private final NuGetToolsInstaller myInstaller;
  private final ToolsRegistry myInstalled;

  public NuGetToolManagerImpl(@NotNull final AvailableToolsState availables,
                              @NotNull final NuGetToolsInstaller installer,
                              @NotNull final ToolsRegistry installed) {
    myAvailables = availables;
    myInstaller = installer;
    myInstalled = installed;
  }

  @NotNull
  public Collection<? extends NuGetInstalledTool> getInstalledTools() {
    return myInstalled.getTools();
  }

  @NotNull
  public Collection<NuGetInstallingTool> getInstallingTool() {
    return Collections.emptyList();
  }

  @NotNull
  public Collection<? extends NuGetTool> getAvailableTools(@NotNull ToolsPolicy policy) throws FetchException {
    //This must be cached to make if work faster!
    return myAvailables.getAvailable(policy);
  }

  public void installTool(@NotNull String toolId) {
    myInstaller.installNuGet(
            toolId,
            (InstallLogger) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{InstallLogger.class},
                    new InvocationHandler() {
                      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        StringBuilder sb = new StringBuilder();
                        sb.append(method.getName());
                        for (Object arg : args) {
                          sb.append(" ").append(arg);
                        }
                        LOG.debug(sb.toString());
                        return null;
                      }
                    }));
  }

  public void removeTool(@NotNull String toolId) {
    myInstalled.removeTool(toolId);
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
}
