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

package jetbrains.buildServer.nuget.agent.runner.pack;

import jetbrains.buildServer.agent.AgentRunningBuild;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.12.11 14:54
 */
public class PackRunnerOutputDirectoryTrackerImpl implements PackRunnerOutputDirectoryTracker {
  private final Map<Long, TrackState> myCache = new ConcurrentHashMap<Long, TrackState>();

  @NotNull
  public TrackState getState(@NotNull final AgentRunningBuild build) {
    final long id = getKey(build);
    TrackState trackState = myCache.get(id);
    if (trackState != null) return trackState;

    trackState = new TrackStateImpl();
    myCache.put(id, trackState);
    return trackState;
  }

  public void removeTrackState(@NotNull final AgentRunningBuild build) {
    myCache.remove(getKey(build));
  }

  private long getKey(@NotNull final AgentRunningBuild build) {
    return build.getBuildId();
  }

  private static class TrackStateImpl implements TrackState {
    private final Map<File, Boolean> myCleanedOutputDirectories = new HashMap<File, Boolean>();

    @NotNull
    public CleanOutcome registerDirectoryClean(@NotNull File dir, boolean cleanEnabled) {
      final Boolean aBoolean = myCleanedOutputDirectories.get(dir);
      if (aBoolean == null) {
        myCleanedOutputDirectories.put(dir, cleanEnabled);
        return cleanEnabled ? CleanOutcome.CLEAN : CleanOutcome.NO_CLEAN_REQUIRED;
      }

      if (Boolean.TRUE.equals(aBoolean)) {
        return CleanOutcome.CLEANED_BEFORE;
      }

      //if (Boolean.FALSE.equals(aBoolean))
      return CleanOutcome.NOT_CLEANED_BEFORE;
    }
  }
}
