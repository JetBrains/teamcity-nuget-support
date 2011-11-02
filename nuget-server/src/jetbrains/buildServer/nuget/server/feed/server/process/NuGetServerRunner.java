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

package jetbrains.buildServer.nuget.server.feed.server.process;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.NetworkUtil;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutor;
import jetbrains.buildServer.nuget.server.exec.NuGetServerHandle;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettings;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerTokens;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 06.10.11 21:05
 */
public class NuGetServerRunner {
  private static final Logger LOG = Logger.getInstance(NuGetServerRunner.class.getName());
  private final NuGetServerRunnerSettings myPaths;
  private final NuGetServerRunnerTokens myTokens;
  private final NuGetExecutor myExecutor;

  private final AtomicReference<NuGetServerHandle> myHandle = new AtomicReference<NuGetServerHandle>();

  public NuGetServerRunner(@NotNull final NuGetServerRunnerSettings paths,
                           @NotNull final NuGetServerRunnerTokens tokens,
                           @NotNull final NuGetExecutor executor) {
    myPaths = paths;
    myTokens = tokens;
    myExecutor = executor;
  }

  public void startServer() {
    stopServer();
    myHandle.set(null);

    final int port = NetworkUtil.getFreePort(32768);
    LOG.info("Allocated NuGet server port: " + port);

    try {
      myHandle.set(
              myExecutor.startNuGetServer(
                      port,
                      myPaths.getPackagesControllerUrl(),
                      myPaths.getLogsPath(),
                      myTokens.getAccessToken()
              ));
    } catch (NuGetExecutionException e) {
      LOG.warn("Failed to start NuGet server. " + e.getMessage(), e);
    }
  }

  public void ensureAlive() {
    final NuGetServerHandle handle = myHandle.get();
    if (handle == null || !handle.isAlive()) {
      LOG.info("NuGet server is down, will be restarted");
      startServer();
    }
  }

  public void stopServer() {
    final NuGetServerHandle handle = myHandle.get();
    if (handle != null) {
      handle.stop();
    }
  }

  @Nullable
  public Integer getPort() {
    final NuGetServerHandle nuGetServerHandle = myHandle.get();
    if (nuGetServerHandle != null) {
      return nuGetServerHandle.getPort();
    }
    return null;
  }
}
