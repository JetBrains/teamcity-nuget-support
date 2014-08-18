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

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Contains settings for packages update parameters
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.07.11 14:02
 */
public interface PackagesUpdateParameters extends NuGetFetchParametersHolder {

  /**
   * @return the way packages are updated
   */
  @NotNull
  PackagesUpdateMode getUpdateMode();

  /**
   * @return true if update should be performed
   *         with -Safe argument
   */
  boolean getUseSafeUpdate();

  /**
   * @return true if update should include prerelease packages
   */
  boolean getIncludePrereleasePackages();

  /**
   * @return list of package Ids to update. Empty list
   *         means update all packages
   */
  @NotNull
  Collection<String> getPackagesToUpdate();

  @NotNull
  Collection<String> getCustomCommandline();
}
