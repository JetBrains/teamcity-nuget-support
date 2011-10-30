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

import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsManager;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:15
 */
public class NuGetSettingsManagerImpl implements NuGetSettingsManager {
  private final ReadWriteLock myLock = new ReentrantReadWriteLock();
  private final AtomicReference<SettingsState> myState = new AtomicReference<SettingsState>(new SettingsState());
  private final List<Runnable> myOnUpdate = new ArrayList<Runnable>();

  @NotNull
  public SettingsState getState() {
    return myState.get();
  }

  public <T> T readSettings(@NotNull NuGetSettingsComponent component, @NotNull Func<NuGetSettingsReader, T> action) {
    myLock.readLock().lock();
    try {
      return action.executeAction(getState().read(component));
    } finally {
      myLock.readLock().unlock();
    }
  }

  public <T> T writeSettings(@NotNull NuGetSettingsComponent component, @NotNull Func<NuGetSettingsWriter, T> action) {
    myLock.writeLock().lock();
    try {
      final ComponentWriter writable = new ComponentWriter(component);
      final T result = action.executeAction(writable);
      myState.set(getState().update(writable));

      for (Runnable runnable : myOnUpdate) {
        runnable.run();
      }

      return result;
    } finally {
      myLock.writeLock().unlock();
    }
  }

  public void reload(@NotNull SettingsState settingsState) {
    myLock.writeLock().lock();
    try {
      myState.set(settingsState);
    } finally {
      myLock.writeLock().unlock();
    }
  }

  public void addOnUpdateHandler(@NotNull final Runnable action) {
    myOnUpdate.add(action);
  }
}
