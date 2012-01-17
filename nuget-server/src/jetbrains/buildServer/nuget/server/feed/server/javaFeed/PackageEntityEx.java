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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed;

import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity.PackageEntityAdapter;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OAtomStreamEntity;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.01.12 12:40
 */
public class PackageEntityEx extends PackageEntityAdapter implements OAtomStreamEntity {
  private final NuGetIndexEntry myEntry;

  public PackageEntityEx(@NotNull final NuGetIndexEntry entry) {
    myEntry = entry;
  }

  public String getAtomEntityType() {
    return "application/zip";
  }

  @Override
  protected String getValue(@NotNull String key) {
    return myEntry.getAttributes().get(key);
  }

  public String getAtomEntitySource(String baseUri) {
    int idx = baseUri.indexOf(ODataPackagesFeedController.PATH);
    if (idx < 0) {
      return null;
    }
    //TODO: check slashes here
    return baseUri.substring(0, idx) + myEntry.getPackageDownloadUrl();
  }
}
