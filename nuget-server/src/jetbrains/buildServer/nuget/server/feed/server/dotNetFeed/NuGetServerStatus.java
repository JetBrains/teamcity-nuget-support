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

package jetbrains.buildServer.nuget.server.feed.server.dotNetFeed;

import org.jetbrains.annotations.NotNull;

/**
 * NuGet Server status snapshot.
 *
 * Use {@link NuGetServerStatusHolder#getStatus()} to recieve a snapshot
 *
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 01.11.11 17:29
 * @see NuGetServerStatusHolder
 */
public interface NuGetServerStatus {
  /**
   * @return true is nuget server is running now
   */
  boolean isRunning();

  /**
   * @return true if nuget server is not running but soon be started
   */
  boolean isScheduledToStart();

  /**
   * @return null if no ping tasts were started or the ping result
   */
  Boolean getServerAccessible();

  /**
   * @return number of latest log lines to be shown on the web
   */
  @NotNull
  String getLogsSlice();
}
