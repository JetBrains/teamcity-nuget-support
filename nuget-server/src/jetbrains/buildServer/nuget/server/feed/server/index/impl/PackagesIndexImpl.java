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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import jetbrains.buildServer.dataStructures.DecoratingIterator;
import jetbrains.buildServer.dataStructures.Mapper;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.transform.*;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 16:18
 */
public class PackagesIndexImpl implements PackagesIndex {
  private final MetadataStorage myStorage;
  private final BuildsManager myBuilds;
  private final ProjectManager myProjects;
  private final SecurityContext myContext;

  public PackagesIndexImpl(@NotNull final MetadataStorage storage,
                           @NotNull final BuildsManager builds,
                           @NotNull final ProjectManager projects,
                           @NotNull final SecurityContext context) {
    myStorage = storage;
    myBuilds = builds;
    myProjects = projects;
    myContext = context;
  }

  @NotNull
  public Iterator<BuildMetadataEntry> getEntries() {
    return myStorage.getAllEntries(NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID);
  }

  @NotNull
  public Iterator<NuGetIndexEntry> getNuGetEntries() {
    final Collection<PackageTransformation> trasformations = Arrays.asList(
            new AddBuildIdTransformation(),
            new SamePackagesFilterTransformation(),
            new OldFormatConvertTransformation(myBuilds),
            new AccessCheckTransformation(myProjects, myContext),
            new IsLatestFieldTransformation(),
            new DownloadUrlComputationTransformation()
    );

    return new DecoratingIterator<NuGetIndexEntry, BuildMetadataEntry>(
            getEntries(),
            new Mapper<BuildMetadataEntry, NuGetIndexEntry>() {
              @Nullable
              public NuGetIndexEntry mapKey(@NotNull BuildMetadataEntry e) {
                final NuGetPackageBuilder pb = new NuGetPackageBuilder(
                        e.getKey(), 
                        e.getBuildId(), 
                        e.getMetadata());

                for (PackageTransformation transformation : trasformations) {
                  if (transformation.applyTransformation(pb) == PackageTransformation.Status.SKIP) return null;
                }
                return pb.build();
              }
            });
  }

}
