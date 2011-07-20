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

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 15:34
 */
public interface TriggerUpdateChecker {
  /**
   * Called from background thread to check for updates
   *
   * Update is called for different trigger settings and thus there should
   * be nothing cached in instance
   *
   * @param descriptor build trigger desciptor to check for parameters
   * @param storage    trigger state
   * @return null or StartReason instance to start a build
   * @throws jetbrains.buildServer.buildTriggers.BuildTriggerException
   *          on error
   */
  @Nullable
  BuildStartReason checkChanges(@NotNull BuildTriggerDescriptor descriptor,
                                @NotNull CustomDataStorage storage) throws BuildTriggerException;
}
