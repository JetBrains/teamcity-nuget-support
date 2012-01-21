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

import jetbrains.buildServer.nuget.server.feed.server.index.impl.transform.*;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 19.10.11 16:18
 */
public class PackagesIndexImpl extends PackagesIndexBase<BuildMetadataEntry> {
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

  @Override
  @NotNull
  public Iterator<BuildMetadataEntry> getEntries() {
    return myStorage.getAllEntries(NuGetArtifactsMetadataProvider.NUGET_PROVIDER_ID);
  }

  @Override
  protected NuGetPackageBuilder builderFromEntry(@NotNull BuildMetadataEntry e) {
    return new NuGetPackageBuilder(
            e.getKey(),
            e.getBuildId(),
            e.getMetadata());
  }

  @NotNull
  @Override
  protected Collection<PackageTransformation> getTransformations() {
    List<PackageTransformation> res = new ArrayList<PackageTransformation>();

    res.add(new AddBuildIdTransformation());
    res.add(new OldFormatConvertTransformation(myBuilds));
    res.add(new AccessCheckTransformation(myProjects, myContext));

    res.addAll(super.getTransformations());

    return res;
  }

  @NotNull
  @Override
  protected DownloadUrlComputationTransformation createDownloadUrlTranslation() {
    return new DownloadUrlComputationTransformation();
  }


}
