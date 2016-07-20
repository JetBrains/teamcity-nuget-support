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

package jetbrains.buildServer.nuget.feed.server.index.impl;

import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.version.SemanticVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

/**
 * Created 18.03.13 15:50
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class SemanticVersionsComparators {
  @NotNull
  public static Comparator<NuGetPackageBuilder> getBuildersComparator() {
    return new Comparator<NuGetPackageBuilder>() {
      public int compare(@NotNull NuGetPackageBuilder o1, @NotNull NuGetPackageBuilder o2) {
        final String id1 = o1.getPackageName();
        final String id2 = o2.getPackageName();

        int cmp;
        if ((cmp = id1.compareTo(id2)) != 0) return cmp;

        final String v1 = o1.getVersion();
        final String v2 = o2.getVersion();
        return SemanticVersion.compareAsVersions(v1, v2);
      }
    };
  }

  @NotNull
  public static Comparator<NuGetIndexEntry> getEntriesComparator() {
    return new Comparator<NuGetIndexEntry>() {
      public int compare(@NotNull NuGetIndexEntry o1, @NotNull NuGetIndexEntry o2) {
        final String id1 = o1.getPackageInfo().getId();
        final String id2 = o2.getPackageInfo().getId();

        int cmp;
        if ((cmp = id1.compareTo(id2)) != 0) return cmp;

        final String v1 = o1.getPackageInfo().getVersion();
        final String v2 = o2.getPackageInfo().getVersion();

        return SemanticVersion.compareAsVersions(v1, v2);
      }
    };
  }
}
