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

package jetbrains.buildServer.nuget.common;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

/**
 * Represent list of downloaded dependecies of a build
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.07.11 11:41
 */
public class PackageDependencies {
  private final Collection<String> mySources;
  private final Collection<PackageInfo> myPackages;

  public PackageDependencies(@NotNull final Collection<String> sources,
                             @NotNull final Collection<PackageInfo> packages) {
    mySources = Collections.unmodifiableCollection(new TreeSet<String>(sources));
    myPackages = Collections.unmodifiableCollection(new TreeSet<PackageInfo>(packages));
  }

  /**
   * @return NuGet sources configured from web
   */
  @NotNull
  public Collection<String> getSources() {
    return mySources;
  }

  /**
   *
   * @return sorted list of packages that were used in project
   */
  @NotNull
  public Collection<PackageInfo> getPackages() {
    return myPackages;
  }

  public boolean isEmpty() {
    return getPackages().isEmpty();
  }
}
