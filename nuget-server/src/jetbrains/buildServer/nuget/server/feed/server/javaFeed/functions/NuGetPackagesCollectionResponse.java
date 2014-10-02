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

package jetbrains.buildServer.nuget.server.feed.server.javaFeed.functions;

import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.MetadataConstants;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OCollection;
import org.odata4j.core.OCollections;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.CollectionResponse;

import java.util.Iterator;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetPackagesCollectionResponse implements CollectionResponse {

  @NotNull private final EdmEntitySet.Builder myPackagesEntitySetBuilder = new EdmEntitySet.Builder().setName(MetadataConstants.ENTITY_SET_NAME);

  private final Iterator<NuGetIndexEntry> myEntries;
  private final EdmType myEntityType;

  public NuGetPackagesCollectionResponse(@NotNull Iterator<NuGetIndexEntry> entries, @NotNull EdmType entityType) {
    myEntries = entries;
    myEntityType = entityType;
  }

  public EdmEntitySet getEntitySet() {
    return myPackagesEntitySetBuilder.build();
  }

  public String getCollectionName() {
    return MetadataConstants.ENTITY_SET_NAME;
  }

  public Integer getInlineCount() {
    return null;
  }

  public String getSkipToken() {
    return null;
  }

  public OCollection getCollection() {
    final OCollection.Builder resultBuilder = OCollections.newBuilder(myEntityType);
    while (myEntries.hasNext()){
      //TODO: create items
    }
    return resultBuilder.build();
  }
}
