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

import jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity.PackageEntity;
import org.core4j.Func;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.exceptions.NotImplementedException;
import org.odata4j.producer.inmemory.InMemoryEdmGenerator;
import org.odata4j.producer.inmemory.InMemoryEntityInfo;
import org.odata4j.producer.inmemory.InMemoryProducer;
import org.odata4j.producer.inmemory.InMemoryTypeMapping;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedInMemoryProducer extends InMemoryProducer {

  public NuGetFeedInMemoryProducer() {
    super(MetadataConstants.NUGET_GALLERY_NAMESPACE);
  }

  public void register(Func<Iterable<PackageEntity>> getFunc){
    register(PackageEntity.class,
            MetadataConstants.ENTITY_SET_NAME,
            MetadataConstants.ENTITY_TYPE_NAME,
            getFunc,
            PackageEntity.KeyPropertyNames);
  }

  @Override
  protected InMemoryEdmGenerator newEdmGenerator(String namespace, InMemoryTypeMapping typeMapping, String idPropName, Map<String, InMemoryEntityInfo<?>> eis) {
    return new NuGetFeedInMemoryEdmGenerator(namespace, MetadataConstants.CONTAINER_NAME, typeMapping, idPropName, eis);
  }

  @Override
  public BaseResponse callFunction(EdmFunctionImport name, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    throw new NotImplementedException();
  }
}
