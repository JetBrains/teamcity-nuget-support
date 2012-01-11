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

package jetbrains.buildServer.nuget.server.feed.server.controllers;

import jetbrains.buildServer.dataStructures.DecoratingIterator;
import jetbrains.buildServer.dataStructures.Mapper;
import jetbrains.buildServer.nuget.server.feed.server.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.entity.PackageEntity;
import jetbrains.buildServer.nuget.server.feed.server.entity.PackageEntityEx;
import org.core4j.Func;
import org.jetbrains.annotations.NotNull;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.xppimpl.XmlPullXMLFactoryProvider2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 19:09
 */
public class NuGetProducer {
  private final InMemoryProducer myProducer;
  private final PackagesIndex myIndex;

  public NuGetProducer(@NotNull final PackagesIndex index) {
    myIndex = index;

    //Workaround for Xml generation. Default STAX xml writer
    //used to generate <foo></foo> that is badly parsed in
    //.NET OData WCF client
    XMLFactoryProvider2.setInstance(new XmlPullXMLFactoryProvider2());

    myProducer = new InMemoryProducer("NuGetGallery");
    myProducer.register(
            PackageEntity.class,
            "Packages",
            "V2FeedPackage",
            new Func<Iterable<PackageEntity>>() {
              public Iterable<PackageEntity> apply() {
                return new Iterable<PackageEntity>() {
                  public Iterator<PackageEntity> iterator() {
                    return new DecoratingIterator<PackageEntity, NuGetIndexEntry>(myIndex.getNuGetEntries(), new Mapper<NuGetIndexEntry, PackageEntity>() {
                      public PackageEntity mapKey(@NotNull NuGetIndexEntry internal) {
                        final Map<String,String> map = new HashMap<String, String>(internal.getAttributes());
                        //Adapt computed values
                        map.put("Created", map.get("LastUpdated"));
                        map.put("Published", map.get("LastUpdated"));
                        map.put("ExternalPackageUri", map.get("ProjectUrl"));
                        map.put("GalleryDetailsUrl", map.get("ProjectUrl"));
                        map.put("Summary", map.get("Description"));
                        map.put("Title", map.get("Id"));
                        map.put("VersionDownloadCount", "42");
                        map.put("DownloadCount", "42");
                        //create package object
                        return new PackageEntityEx(map);
                      }
                    });
                  }
                };
              }
            },
            PackageEntity.KeyPropertyNames
    );
  }

  @NotNull
  public ODataProducer getProducer() {
    return myProducer;
  }
}
