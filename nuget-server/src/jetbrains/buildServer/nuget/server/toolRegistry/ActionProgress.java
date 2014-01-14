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

package jetbrains.buildServer.nuget.server.toolRegistry;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for showing progress messages from
 * log-working UI actions
 *
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.08.11 20:26
 */
public interface ActionProgress {
  /**
   * Call this method from a task to provide user with new
   * status message text
   * @param kind message kind
   * @param message text of message to show
   */
  void addProgressMessage(@NotNull Kind kind, @NotNull String message);

  enum Kind {
    ERROR,
    NORMAL
  }
}
