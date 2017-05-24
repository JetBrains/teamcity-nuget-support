/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.trigger.impl.queue;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.trigger.impl.checker.PackageChecker;
import jetbrains.buildServer.nuget.server.trigger.impl.settings.PackageCheckerSettings;
import jetbrains.buildServer.nuget.server.trigger.impl.source.NuGetSourceChecker;
import jetbrains.buildServer.util.NamedDaemonThreadFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 14:30
 */
public class PackageChangesCheckerThread {
  private static final Logger LOG = Logger.getInstance(PackageChangesCheckerThread.class.getName());

  private final ScheduledExecutorService myExecutor;
  private final PackageCheckQueue myHolder;
  private final Collection<PackageChecker> myCheckers;
  private final NuGetSourceChecker myPreChecker;

  public PackageChangesCheckerThread(@NotNull final PackageCheckQueue holder,
                                     @NotNull final PackageCheckerSettings settings,
                                     @NotNull final Collection<PackageChecker> checkers,
                                     @NotNull final NuGetSourceChecker preChecker) {
    myHolder = holder;
    myCheckers = checkers;
    myPreChecker = preChecker;
    myExecutor = Executors.newScheduledThreadPool(settings.getCheckerThreads(), new NamedDaemonThreadFactory("NuGet Packages Version Checker"));
  }

  public void stopPackagesCheck() {
    myExecutor.shutdownNow();
    try {
      myExecutor.awaitTermination(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOG.debug("Interrupted wait of NuGet packages checker executor service shutdown. ", e);
    }
  }

  public void startPackagesCheck() {
    new PackageChangesCheckerThreadTask(myHolder, myExecutor, myCheckers, myPreChecker).postCheckTask();
  }
}
