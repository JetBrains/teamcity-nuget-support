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

import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettings;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerStatus;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerStatusHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 01.11.11 17:09
 */
public class NuGetServerStatusHolderImpl implements NuGetServerStatusHolder {
  private final AtomicReference<State> myState = new AtomicReference<State>(INITIAL_STATE);
  private final NuGetServerRunnerSettings mySettings;

  public NuGetServerStatusHolderImpl(@NotNull final NuGetServerRunnerSettings settings) {
    mySettings = settings;
  }

  @NotNull
  public NuGetServerStatus getStatus() {
    final State state = getState();
    final boolean isEnabled = mySettings.isNuGetFeedEnabled();

    return new NuGetServerStatus() {
      public boolean isRunning() {
        return state.isRunning();
      }

      public boolean isScheduledToStart() {
        return !isRunning() && isEnabled;
      }

      public Boolean getServerAccessible() {
        return state.isServerAccessible();
      }

      @NotNull
      public Collection<String> getLogsSlice() {
        return NuGetServerStatusHolderImpl.this.getLogsSlice();
      }
    };
  }

  @NotNull
  private State getState() {
    return myState.get();
  }

  public boolean isRunning() {
    return getState().isRunning();
  }

  public Boolean isServerAccessible() {
    return getState().isServerAccessible();
  }

  @NotNull
  public Collection<String> getLogsSlice() {
    //TODO
    return Collections.emptyList();
  }

  public void startingServer() {
    myState.set(getState().setRunning());
  }

  public void stoppingServer() {
    myState.set(getState().setStopping());
  }

  public void pingSucceeded() {
    myState.set(getState().setPingSucceeded());
  }

  public void pingFailed() {
    myState.set(getState().setPingFailed());
  }

  public void setRunning() {
    startingServer();
  }

  private static class State {
    private final boolean myIsRunning;
    private final Boolean myIsServerAccessible;

    private State(boolean isRunning, @Nullable Boolean isServerAccessible) {
      myIsRunning = isRunning;
      myIsServerAccessible = isServerAccessible;
    }

    public boolean isRunning() {
      return myIsRunning;
    }

    @Nullable
    public Boolean isServerAccessible() {
      return myIsServerAccessible;
    }

    public State setRunning() {
      return new State(true, myIsServerAccessible);
    }

    public State setStopping() {
      return new State(false, myIsServerAccessible);
    }

    public State setPingSucceeded() {
      return new State(isRunning(), true);
    }

    public State setPingFailed() {
      return new State(isRunning(), false);
    }
  }

  private static final State INITIAL_STATE = new State(false, null);
}
