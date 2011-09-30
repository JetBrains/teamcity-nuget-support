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

import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledBuildTrigger;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.07.11 23:29
 */
public class ThreadedBuildTriggerPolicy extends PolledBuildTrigger {
  private final TriggerUpdateChecker myUpdater;

  public ThreadedBuildTriggerPolicy(@NotNull final TriggerUpdateChecker updater) {
    myUpdater = updater;
  }

  @Override
  public synchronized void triggerBuild(@NotNull PolledTriggerContext context) throws BuildTriggerException {
    final BuildStartReason result = myUpdater.checkChanges(context.getTriggerDescriptor(), context.getCustomDataStorage());

    if (result != null) {
      context.getBuildType().addToQueue(result.getReason());
    }
  }
}
