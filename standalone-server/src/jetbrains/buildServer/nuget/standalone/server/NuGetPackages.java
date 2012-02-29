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
import com.intellij.util.text.VersionComparatorUtil;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FilesWatcher;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackageFile;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackageLoadException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.02.12 0:00
 */
public class NuGetPackages {
  private static final Logger LOG = Logger.getInstance(NuGetPackages.class.getName());

  private final AtomicReference<Collection<Entry>> myPackagesCache = new AtomicReference<Collection<Entry>>();
  private final FilesWatcher myWatcher;
  @NotNull
  private final ServerSettings mySettings;

  public NuGetPackages(@NotNull final ServerSettings settings) {
    mySettings = settings;
    myWatcher = new FilesWatcher(new FilesWatcher.WatchedFilesProvider() {
      public File[] getWatchedFiles() {
        final File[] files = settings.getPackagesFolder().listFiles(PACKAGES_FILTER);
        return files == null ? new File[0] : files;
      }
    });

    myWatcher.registerListener(new ChangeListener() {
      public void changeOccured(String requestor) {
        //TODO: use myWatcher.{added, removed, changed} to work faster
        reloadPackagesIfNeeded();
      }
    });

    myWatcher.setSleepingPeriod(settings.getPackagesRefreshInterval());

    reloadPackagesIfNeeded();
    myWatcher.start();
  }

  @NotNull
  protected Iterator<Entry> getEntries() {
    final Collection<Entry> entries = myPackagesCache.get();
    if (entries != null) return entries.iterator();
    return Collections.<Entry>emptyList().iterator();
  }

  private synchronized void reloadPackagesIfNeeded() {
    int id = 42;
    final LocalNuGetPackageItemsFactory factory = new LocalNuGetPackageItemsFactory();
    final File packagesFolder = mySettings.getPackagesFolder();
    final File[] file = packagesFolder.listFiles(PACKAGES_FILTER);
    if (file == null) {
      System.out.println("Failed to read packages folder contents: " + packagesFolder);
      return;
    }

    final List<Entry> result = new ArrayList<Entry>();
    for (final File ch : file) {
      if (ch.isFile() && ch.getName().endsWith(".nupkg")) {
        final Map<String, String> map = loadPackage(factory, ch);
        if (map != null) {
          result.add(new Entry(++id, map));
        }
      }
    }

    Collections.sort(result, new Comparator<Entry>() {
      public int compare(Entry o1, Entry o2) {
        final String v1 = o1.getVersion();
        final String v2 = o2.getVersion();

        if (v1 == null && v2 != null) return 1;
        if (v1 != null && v2 == null) return -1;
        //noinspection ConstantConditions
        if (v1 == null && v2 == null) return 0;

        return -VersionComparatorUtil.compare(v1, v2);
      }
    });

    myPackagesCache.set(result);
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

  private final FilenameFilter PACKAGES_FILTER = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name.toLowerCase().endsWith(".nupkg");
    }
  };
}
