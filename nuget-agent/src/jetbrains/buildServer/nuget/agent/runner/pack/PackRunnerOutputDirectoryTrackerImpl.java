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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    trackState = new TrackState();
    myCache.put(id, trackState);
    return trackState;
  }

  public void removeTrackState(@NotNull final AgentRunningBuild build) {
    myCache.remove(getKey(build));
  }

  private long getKey(@NotNull final AgentRunningBuild build) {
    return build.getBuildId();
  }

  private static class TrackState {
    private final Set<File> myCleanedOutputDirectories = new HashSet<File>();

    /**
     * Registers directory clean
     * @param dir directory to clean
     * @return true if this was first directory clean attempt
     */
    public boolean addDirectoryToClean(@NotNull final File dir) {
      return myCleanedOutputDirectories.add(dir);
    }
  }
}
