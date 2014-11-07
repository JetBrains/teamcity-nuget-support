/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import jetbrains.buildServer.dataStructures.DecoratingIterator;
import jetbrains.buildServer.dataStructures.Mapper;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity.PackageEntity;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.functions.NuGetFeedFunctions;
import org.core4j.Func;
import org.jetbrains.annotations.NotNull;
import org.odata4j.producer.ODataProducer;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.xppimpl.XmlPullXMLFactoryProvider2;

import java.util.Iterator;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 19:09
 */
public class NuGetProducerHolder {
  private final NuGetFeedInMemoryProducer myProducer;

  public NuGetProducerHolder(@NotNull final PackagesIndex index,
                             @NotNull final NuGetServerSettings settings,
                             @NotNull final NuGetFeedFunctions functions) {
    //Workaround for Xml generation. Default STAX xml writer
    //used to generate <foo></foo> that is badly parsed in
    //.NET OData WCF client
    XMLFactoryProvider2.setInstance(new XmlPullXMLFactoryProvider2());
    myProducer = new NuGetFeedInMemoryProducer(functions);
    myProducer.register(new Func<Iterable<PackageEntity>>() {
      public Iterable<PackageEntity> apply() {
        return new Iterable<PackageEntity>() {
          public Iterator<PackageEntity> iterator() {
            return new DecoratingIterator<PackageEntity, NuGetIndexEntry>(index.getNuGetEntries(), new Mapper<NuGetIndexEntry, PackageEntity>() {
              public PackageEntity mapKey(@NotNull NuGetIndexEntry internal) {
                return new PackageEntityEx(internal, settings);
              }
            });
          }
        };
      }
    });
  }

  @NotNull
  public ODataProducer getProducer() {
    return myProducer;
  }
}
