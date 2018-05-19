/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server.odata4j;

import jetbrains.buildServer.nuget.feed.server.NuGetUtils;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.feed.server.odata4j.entity.PackageEntityAdapter;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OAtomStreamEntity;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.01.12 12:40
 */
public class PackageEntityEx extends PackageEntityAdapter implements OAtomStreamEntity {

  private static final String APPLICATION_ZIP = "application/zip";

  private final NuGetIndexEntry myEntry;

  public PackageEntityEx(@NotNull final NuGetIndexEntry entry) {
    myEntry = entry;
  }

  public String getAtomEntityType() {
    return APPLICATION_ZIP;
  }

  public String getAtomEntitySource(String baseUri) {
    int idx = baseUri.indexOf(NuGetUtils.getProjectNuGetFeedPath(myEntry.getFeedName()));
    if (idx < 0) {
      return null;
    }
    //TODO: check slashes here
    return baseUri.substring(0, idx) + myEntry.getPackageDownloadUrl();
  }

  @Override
  protected String getValue(@NotNull String key) {
    return NuGetUtils.getValue(myEntry.getAttributes(), key);
  }
}
