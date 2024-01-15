

package jetbrains.buildServer.nuget.server.settings.impl;

import jetbrains.buildServer.nuget.server.settings.*;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

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
  private final EventDispatcher<NuGetSettingsEventHandler> myDispatcher = EventDispatcher.create(NuGetSettingsEventHandler.class);

  @NotNull
  public SettingsState getState() {
    return myState.get();
  }

  public void addListener(@NotNull NuGetSettingsEventHandler eventListener) {
    myDispatcher.addListener(eventListener);
  }

  public void removeListener(@NotNull NuGetSettingsEventHandler eventListener) {
    myDispatcher.removeListener(eventListener);
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
    final T result;
    try {
      final ComponentWriter writable = new ComponentWriter(component);
      result = action.executeAction(writable);
      myState.set(getState().update(writable));
    } finally {
      myLock.writeLock().unlock();
    }
    myDispatcher.getMulticaster().settingsChanged(component);
    return result;
  }

  public void reload(@NotNull SettingsState settingsState) {
    myLock.writeLock().lock();
    try {
      myState.set(settingsState);
    } finally {
      myLock.writeLock().unlock();
    }

    myDispatcher.getMulticaster().settingsReloaded();
  }
}
