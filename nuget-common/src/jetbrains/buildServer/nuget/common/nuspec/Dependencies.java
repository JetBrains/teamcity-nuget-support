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

package jetbrains.buildServer.nuget.common.nuspec;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Evgeniy.Koshkin
 */
public class Dependencies {
  private final Collection<Dependency> myDependencies;
  private final Collection<DependencyGroup> myGroups;

  public Dependencies(@NotNull Collection<Dependency> dependencies, @NotNull Collection<DependencyGroup> groups) {
    myDependencies = dependencies;
    myGroups = groups;
  }

  @NotNull
  public Collection<Dependency> getDependencies() {
    return myDependencies;
  }

  @NotNull
  public Collection<DependencyGroup> getGroups() {
    return myGroups;
  }
}
