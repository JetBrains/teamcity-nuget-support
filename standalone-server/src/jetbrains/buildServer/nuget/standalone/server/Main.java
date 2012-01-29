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

import com.sun.jersey.api.container.filter.LoggingFilter;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.NuGetODataApplication;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.NuGetProducerBase;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity.PackageEntity;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity.PackageEntryData;
import org.jetbrains.annotations.NotNull;
import org.odata4j.jersey.producer.server.JerseyServer;
import org.odata4j.producer.resources.RootApplication;

import java.util.Iterator;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 29.01.12 23:18
 */
public class Main {
  public static void main(String[] args) {
    System.out.println("NuGet Feed server.");
    final String appBaseUri = "http://localhost:9878";
    final JerseyServer server = new JerseyServer(
            appBaseUri,
            NuGetApp.class,
            RootApplication.class);

    server.addJerseyRequestFilter(LoggingFilter.class);
    server.start();
    System.out.println("Server started at " + appBaseUri);
  }

  public static class NuGetApp extends NuGetODataApplication {
    public NuGetApp() {
      super(new NuGetProducerBase(new PackagesIndex() {
            @NotNull
            public Iterator<NuGetIndexEntry> getNuGetEntries() {
              return new Iterator<NuGetIndexEntry>() {
                public boolean hasNext() {
                  return false;
                }

                public NuGetIndexEntry next() {
                  return null;
                }

                public void remove() {
                }
              };
            }
          }) {
        @Override
        protected PackageEntity createODataPackageEntry(@NotNull NuGetIndexEntry internal) {
          return new PackageEntryData(internal) {
            @Override
            public String getAtomEntitySource(String baseUri) {
              return null;
            }
          };
        }
      });
    }
  }
}
