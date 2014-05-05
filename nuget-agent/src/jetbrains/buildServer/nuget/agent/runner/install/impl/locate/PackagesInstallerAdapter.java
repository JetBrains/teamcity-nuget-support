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

package jetbrains.buildServer.nuget.agent.runner.install.impl.locate;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.util.EventListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 12:15
 */
@EventListenerAdapter
public class PackagesInstallerAdapter implements PackagesInstallerCallback {
  public void onPackagesConfigFound(@NotNull File config, @NotNull File repositoryPath) throws RunBuildException {
  }

  public void onNoPackagesConfigsFound() throws RunBuildException {
  }

  public void onSolutionFileFound(@NotNull File sln, @NotNull File repositoryPath) throws RunBuildException {
  }
}
