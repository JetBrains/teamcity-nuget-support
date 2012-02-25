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

package jetbrains.buildServer.nuget.standalone.server;

import jetbrains.buildServer.nuget.server.feed.server.javaFeed.NuGetODataApplication;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 29.01.12 23:41
*/
public class NuGetApplication extends NuGetODataApplication {
  @NotNull private final ServerSettings mySettings;

  public NuGetApplication() {
    this(NuGetServerMain.getSettings());
  }

  private NuGetApplication(@NotNull final ServerSettings settings) {
    super(new NuGetApplicationProducer(settings));
    mySettings = settings;
  }

  @Override
  public Set<Object> getSingletons() {
    final Set<Object> list = new HashSet<Object>(super.getSingletons());
    list.add(new PackageDownload(mySettings.getPackagesFolder()));
    return list;
  }
}
