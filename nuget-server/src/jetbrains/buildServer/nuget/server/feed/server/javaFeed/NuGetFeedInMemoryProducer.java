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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity.PackageEntity;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.functions.NuGetFeedFunction;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.functions.NuGetFeedFunctions;
import org.core4j.Func;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.ODataContext;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.inmemory.*;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedInMemoryProducer extends InMemoryProducer {

  private final Logger LOG = Logger.getInstance(getClass().getName());
  private final NuGetFeedFunctions myFunctions;

  public NuGetFeedInMemoryProducer(@NotNull NuGetFeedFunctions functions) {
    super(MetadataConstants.NUGET_GALLERY_NAMESPACE);
    myFunctions = functions;
  }

  public void register(Func<Iterable<PackageEntity>> getFunc){
    register(PackageEntity.class,
            MetadataConstants.ENTITY_SET_NAME,
            MetadataConstants.ENTITY_TYPE_NAME,
            getFunc,
            PackageEntity.KeyPropertyNames);
  }

  @Override
  protected InMemoryEdmGenerator newEdmGenerator(String namespace, InMemoryTypeMapping typeMapping, String idPropName, Map<String, InMemoryEntityInfo<?>> eis, Map<String, InMemoryComplexTypeInfo<?>> complexTypesInfo) {
    return new NuGetFeedInMemoryEdmGenerator(namespace, MetadataConstants.CONTAINER_NAME, typeMapping, idPropName, eis, complexTypesInfo, myFunctions);
  }

  @Override
  public BaseResponse callFunction(ODataContext context, EdmFunctionImport name, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    final NuGetFeedFunction targetFunction = myFunctions.find(name);
    if(targetFunction == null){
      LOG.debug("Failed to process NuGet feed function call. Failed to find target function by name " + name.getName());
      throw new NotImplementedException();
    }
    return targetFunction.call(name.getReturnType(), params, queryInfo);
  }
}
