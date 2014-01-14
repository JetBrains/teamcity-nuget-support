/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed.cache;

import jetbrains.buildServer.serverSide.GeneralDataCleaner;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.04.13 9:52
 */
public class CleanupCacheReset implements GeneralDataCleaner {
  private final ResponseCacheReset myReset;

  public CleanupCacheReset(@NotNull ResponseCacheReset reset) {
    myReset = reset;
  }

  public void performCleanup(@NotNull Connection _) throws SQLException {
    myReset.resetCache();
  }
}
