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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.07.11 23:29
 */
public class ThreadedBuildTriggerPolicy extends PolledBuildTrigger {
  private final ExecutorService myExecutor;
  private final TriggerUpdateChecker myUpdater;
  private Future<BuildStartReason> myUpdateRequired;

  public ThreadedBuildTriggerPolicy(@NotNull final ExecutorService executor,
                                    @NotNull final TriggerUpdateChecker updater) {
    myExecutor = executor;
    myUpdater = updater;
  }

  @Override
  public synchronized void triggerBuild(@NotNull PolledTriggerContext context) throws BuildTriggerException {
    if (myUpdateRequired == null) {
      myUpdateRequired = myExecutor.submit(createUpdateTask(context));
    }

    if (!myUpdateRequired.isDone()) return;

    BuildStartReason result;
    try {
      result = myUpdateRequired.get();
    } catch (InterruptedException e) {
      return;
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof BuildTriggerException) throw (BuildTriggerException)cause;
      throw new BuildTriggerException(cause.getMessage(), cause);
    } finally {
      myUpdateRequired = null;
    }

    if (result != null) {
      context.getBuildType().addToQueue(result.getReason());
    }
  }

  @NotNull
  private Callable<BuildStartReason> createUpdateTask(@NotNull final PolledTriggerContext storage) {
    return new Callable<BuildStartReason>() {
      public BuildStartReason call() throws Exception {
        return myUpdater.checkChanges(storage.getTriggerDescriptor(), storage.getCustomDataStorage());
      }
    };
  }
}
