/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import jetbrains.buildServer.dataStructures.DecoratingIterator;
import jetbrains.buildServer.dataStructures.Mapper;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.transform.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.01.12 15:02
 */
public abstract class PackagesIndexBase<TEntry> implements PackagesIndex {
  @NotNull
  protected abstract Iterator<TEntry> getEntries();

  @Nullable
  protected abstract NuGetPackageBuilder builderFromEntry(@NotNull final TEntry entry);

  @NotNull
  public Iterator<NuGetIndexEntry> getNuGetEntries() {
    final Collection<PackageTransformation> trasformations = getTransformations();
    return new DecoratingIterator<NuGetIndexEntry, TEntry>(
            getEntries(),
            new Mapper<TEntry, NuGetIndexEntry>() {
              @Nullable
              public NuGetIndexEntry mapKey(@NotNull TEntry e) {
                final NuGetPackageBuilder pb = builderFromEntry(e);
                if (pb == null) return null;

                for (PackageTransformation transformation : trasformations) {
                  if (transformation.applyTransformation(pb) == PackageTransformation.Status.SKIP) return null;
                }
                return pb.build();
              }
            });
  }

  @NotNull
  protected Collection<PackageTransformation> getTransformations() {
    return Arrays.asList(
            new SamePackagesFilterTransformation(),
            createIsLatestTransformation(),
            createDownloadUrlTranslation()
    );
  }

  @NotNull
  protected abstract DownloadUrlComputationTransformation createDownloadUrlTranslation();

  @NotNull
  protected abstract IsLatestFieldTransformationBase createIsLatestTransformation();
}
