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
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 18:26
 */
public class NuGetServerCruiserTask {
  private static final Logger LOG = Logger.getInstance(NuGetServerCruiserTask.class.getName());
  private final NuGetServerRunnerSettings mySettings;
  private final NuGetServerStatusHolderImpl myStatus;
  private final NuGetServerPing myPing;
  private final NuGetServerRunner myRunner;

  private volatile String mySettingsHash;


  public NuGetServerCruiserTask(@NotNull final NuGetServerRunnerSettings settings,
                                @NotNull final NuGetServerStatusHolderImpl status,
                                @NotNull final NuGetServerPing ping,
                                @NotNull final NuGetServerRunner runner) {
    mySettings = settings;
    myStatus = status;
    myPing = ping;
    myRunner = runner;

    mySettingsHash = mySettings.getSettingsHash();
  }

  public void checkNuGetServerState() {
    if (!mySettingsHash.equals(mySettingsHash = mySettings.getSettingsHash())) {
      LOG.info("Settings were changed. NuGet server will be restarted.");
      myRunner.stopServer();
    }

    if (mySettings.isNuGetFeedEnabled()) {
      myRunner.ensureAlive();
      myStatus.setRunning();

      if (!myPing.pingNuGetServer()) {
        myStatus.pingFailed();
        LOG.warn("Failed to ping NuGet server. Server will be restarted.");
        myRunner.stopServer();
      } else {
        myStatus.pingSucceeded();
      }
    } else {
      myStatus.stoppingServer();
      myRunner.stopServer();
    }
  }
}
