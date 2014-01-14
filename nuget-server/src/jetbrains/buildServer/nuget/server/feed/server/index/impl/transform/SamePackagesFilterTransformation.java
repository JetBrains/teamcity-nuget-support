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

package jetbrains.buildServer.nuget.server.feed.server.index.impl.transform;

import jetbrains.buildServer.nuget.server.feed.server.index.impl.NuGetPackageBuilder;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackageTransformation;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 18.01.12 20:29
*/
public class SamePackagesFilterTransformation implements PackageTransformation {
  private final Set<String> reportedPackages = new HashSet<String>();

  @NotNull
  public Status applyTransformation(@NotNull NuGetPackageBuilder builder) {
    return reportedPackages.add(builder.getKey()) ? Status.CONTINUE : Status.SKIP;
  }

  @NotNull
  public PackageTransformation createCopy() {
    return new SamePackagesFilterTransformation();
  }
}
