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

package jetbrains.buildServer.nuget.agent.runner;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created 28.12.12 18:44
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class EnabledPackagesOptionSetter {
  public static final String ENABLE_NUGET_PACKAGE_RESTORE = "EnableNuGetPackageRestore";

  public EnabledPackagesOptionSetter(@NotNull EventDispatcher<AgentLifeCycleListener> events) {
    final Set<String> nugetTypes = new HashSet<String>(Arrays.asList(PackagesConstants.ALL_NUGET_RUN_TYPES));

    events.addListener(new AgentLifeCycleAdapter(){
      @Override
      public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        if (!nugetTypes.contains(runner.getRunType())) return;
        if (runner.getBuildParameters().getEnvironmentVariables().containsKey(ENABLE_NUGET_PACKAGE_RESTORE)) return;

        runner.addEnvironmentVariable(ENABLE_NUGET_PACKAGE_RESTORE, "True");
      }
    });
  }
}
