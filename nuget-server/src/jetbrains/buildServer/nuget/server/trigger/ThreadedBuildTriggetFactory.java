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

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.buildTriggers.BuildTriggeringPolicy;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for thread-based build triggers
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 15:25
 */
public interface ThreadedBuildTriggetFactory {

  /**
   * Creates background thread powered BuildTriggerPolicy implamentation
   * for a given TriggerUpdateChecker.
   * @param updateChecker checker that would be called from background thread
   * @return build triggering policy
   */
  public BuildTriggeringPolicy createTrigger(@NotNull TriggerUpdateChecker updateChecker);

}
