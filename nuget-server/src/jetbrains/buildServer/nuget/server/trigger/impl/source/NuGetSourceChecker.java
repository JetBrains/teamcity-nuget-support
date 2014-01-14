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

package jetbrains.buildServer.nuget.server.trigger.impl.source;

import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.07.12 13:09
 * @since 7.1
 */
public interface NuGetSourceChecker {
  /**
   * Checks all package sources from the collection.
   * Mehod returns only packages with sources that are accessible.
   * All other sources will be updated by the checker.
   *
   * @param allPackages all packages to check
   * @return only accessible packages
   */
  @NotNull
  Collection<CheckablePackage> getAccessiblePackages(@NotNull Collection<CheckablePackage> allPackages);
}
