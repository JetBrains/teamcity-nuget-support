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

import java.util.concurrent.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.07.11 23:29
 */
public class NuGetTriggerPolicy extends PolledBuildTrigger{
  private final ExecutorService myExecutor;
  private Future<Boolean> myUpdateRequired;

  public NuGetTriggerPolicy(@NotNull final ExecutorService executor) {
    myExecutor = executor;
  }

  @Override
  public synchronized void triggerBuild(@NotNull PolledTriggerContext context) throws BuildTriggerException {
    if (myUpdateRequired == null) {
      myUpdateRequired = myExecutor.submit(createUpdateTask());
    }

    if (!myUpdateRequired.isDone()) return;

    Boolean result;
    try {
      result = myUpdateRequired.get();
    } catch (InterruptedException e) {
      return;
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      throw new BuildTriggerException(cause.getMessage(), cause);
    } finally {
      myUpdateRequired = null;
    }

    if (Boolean.TRUE.equals(result)) {
      String packageName = context.getTriggerDescriptor().getProperties().get(TriggerConstants.PACKAGE);
      context.getBuildType().addToQueue("NuGet Package " + packageName + " updated");
    }
  }

  private Callable<Boolean> createUpdateTask() {
    return new Callable<Boolean>() {
      public Boolean call() throws Exception {
        return null;
      }
    };
  }


}
