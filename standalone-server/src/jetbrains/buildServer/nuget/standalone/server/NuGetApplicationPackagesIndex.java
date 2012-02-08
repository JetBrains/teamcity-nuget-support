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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.*;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.transform.IsLatestCalculator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 29.01.12 23:42
 */
public class NuGetApplicationPackagesIndex extends PackagesIndexBase<Entry> {
  private static final Logger LOG = Logger.getInstance(NuGetApplicationPackagesIndex.class.getName());

  @NotNull
  @Override
  protected Iterator<Entry> getEntries() {
    int id = 42;
    final LocalNuGetPackageItemsFactory factory = new LocalNuGetPackageItemsFactory();
    final File[] file = Main.getPackagesRoot().listFiles();
    if (file == null) return Collections.<Entry>emptyList().iterator();
    final List<Entry> result = new ArrayList<Entry>();
    for (final File ch : file) {
      if (ch.isFile() && ch.getName().endsWith(".nupkg")) {
        final Map<String, String> map = loadPackage(factory, ch);
        if (map != null) {
          result.add(new Entry(++id, map));
        }
      }
    }
    return result.iterator();
  }

  @Nullable
  private Map<String, String> loadPackage(LocalNuGetPackageItemsFactory factory, final File ch) {
    try {
      return factory.loadPackage(file(ch));
    } catch (PackageLoadException e) {
      LOG.warn("Failed to load package: " + ch);
      return null;
    }
  }

  private PackageFile file(final File ch) {
    return new PackageFile() {
      public long getSize() {
        return ch.length();
      }

      @NotNull
      public Date getLastUpdated() {
        return new Date(ch.lastModified());
      }

      @NotNull
      public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(ch));
      }
    };
  }

  @Override
  protected NuGetPackageBuilder builderFromEntry(@NotNull Entry entry) {
    final NuGetPackageBuilder builder = new NuGetPackageBuilder(entry.getKey(), entry.getId(), entry.getMap());
    builder.setBuildTypeId("not-tc");
    return builder;
  }

  @Override
  protected String getDownloadUrl(@NotNull NuGetPackageBuilder builder) {
    return "/download/" + builder.getKey();
  }

  @NotNull
  @Override
  protected IsLatestCalculator createIsLatestTransformation() {
    return new IsLatestCalculator() {
      public Boolean isLatest(@NotNull NuGetPackageBuilder builder) {
        //TODO: implement is latest computation
        return false;
      }
    };
  }
}
