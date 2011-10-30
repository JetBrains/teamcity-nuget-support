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

package jetbrains.buildServer.nuget.server.settings.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 15:32
 */
public class NuGetSettingsWatcher {
  private static final Logger LOG = Logger.getInstance(NuGetSettingsWatcher.class.getName());
  private final FileWatcher myWatcher;

  public NuGetSettingsWatcher(@NotNull final NuGetSettingsManagerConfiguration configuration,
                              @NotNull final EventDispatcher<BuildServerListener> events,
                              @NotNull final NuGetSettingsPersistance persist,
                              @NotNull final NuGetSettingsManagerImpl settings) {
    myWatcher = new FileWatcher(configuration.getNuGetConfigXml());
    myWatcher.setSleepingPeriod(1000 * 60 * 5);

    events.addListener(new BuildServerAdapter() {
      @Override
      public void serverStartup() {
        myWatcher.start();
        reloadSettings(settings, persist);
      }

      @Override
      public void serverShutdown() {
        myWatcher.stop();
      }
    });

    myWatcher.registerListener(new ChangeListener() {
      public void changeOccured(String requestor) {
        reloadSettings(settings, persist);
      }
    });

    settings.addOnUpdateHandler(new Runnable() {
      public void run() {
        myWatcher.runActionWithDisabledObserver(new Runnable() {
          public void run() {
            try {
              persist.saveSettings(settings.getState());
            } catch (IOException e) {
              LOG.warn("Failed to save NuGet settings. " + e.getMessage());
              LOG.debug("Failed to save NuGet settings. " + e.getMessage(), e);
            }
          }
        });
      }
    });
  }

  private void reloadSettings(NuGetSettingsManagerImpl settings, NuGetSettingsPersistance persist) {
    try {
      settings.reload(persist.loadSettings());
    } catch (IOException e) {
      LOG.warn("Failed to update NuGet settings. " + e.getMessage());
      LOG.debug("Failed to update NuGet settings. " + e.getMessage(), e);
    }
  }

  public void setWatchInterval(long interval) {
    myWatcher.setSleepingPeriod(interval);
  }
}
