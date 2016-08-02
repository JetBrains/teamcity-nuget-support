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

package jetbrains.buildServer.nuget.feed.server.odata4j;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.feed.server.MetadataConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feed.server.odata4j.entity.PackageEntity;
import jetbrains.buildServer.nuget.feed.server.odata4j.functions.NuGetFeedFunction;
import jetbrains.buildServer.nuget.feed.server.odata4j.functions.NuGetFeedFunctions;
import org.core4j.Enumerable;
import org.core4j.Func;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.ODataContext;
import org.odata4j.producer.PropertyPathHelper;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.inmemory.*;

import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedInMemoryProducer extends InMemoryProducer {
  private static final Logger LOG = Logger.getInstance(NuGetFeedInMemoryProducer.class.getName());
  private final Object mySyncRoot = new Object();

  @NotNull private final NuGetFeedFunctions myFunctions;

  private String myApiVersion;

  public NuGetFeedInMemoryProducer(@NotNull final NuGetFeedFunctions functions) {
    super(MetadataConstants.NUGET_GALLERY_NAMESPACE);
    myFunctions = functions;
  }

  public void register(Func<Iterable<PackageEntity>> getFunc){
    register(PackageEntity.class,
            MetadataConstants.ENTITY_SET_NAME,
            NuGetAPIVersion.getVersionToUse() + MetadataConstants.ENTITY_TYPE_NAME,
            getFunc,
            PackageEntity.KeyPropertyNames);
  }

  @Override
  public EdmDataServices getMetadata() {
    final String apiVersionToUse = NuGetAPIVersion.getVersionToUse();
    synchronized (mySyncRoot){
      if(!apiVersionToUse.equalsIgnoreCase(myApiVersion)){
        cleanCachedMetadata();
        myApiVersion = apiVersionToUse;
      }
    }
    return super.getMetadata();
  }

  @Override
  public BaseResponse callFunction(ODataContext context, EdmFunctionImport function, Map<String, OFunctionParameter> params, QueryInfo queryInfo, boolean isCountCall) {
    final NuGetFeedFunction targetFunction = myFunctions.find(function);
    if(targetFunction == null){
      LOG.debug("Failed to process NuGet feed function call. Failed to find target function by name " + function.getName());
      throw new NotImplementedException();
    }

    final Iterable<Object> functionCallResult = targetFunction.call(function.getReturnType(), params, queryInfo);
    if(functionCallResult == null) return null;

    final RequestContext rc = RequestContext.newBuilder(RequestContext.RequestType.GetEntities)
            .entitySetName(MetadataConstants.ENTITY_SET_NAME)
            .entitySet(getMetadata().getEdmEntitySet(MetadataConstants.ENTITY_SET_NAME))
            .queryInfo(queryInfo)
            .odataContext(context)
            .pathHelper(new PropertyPathHelper(queryInfo)).build();
    final InMemoryEntityInfo<?> ei = getEntityInfo(MetadataConstants.ENTITY_SET_NAME);

    if(isCountCall)
      return getCountResponse(rc, Enumerable.create(functionCallResult));
    else
      return getEntitiesResponse(rc, rc.getEntitySet(), Enumerable.create(functionCallResult), ei.getPropertyModel());
  }

  @Override
  protected InMemoryEdmGenerator newEdmGenerator(String namespace, InMemoryTypeMapping typeMapping, String idPropName, Map<String, InMemoryEntityInfo<?>> eis, Map<String, InMemoryComplexTypeInfo<?>> complexTypesInfo) {
    return new NuGetFeedInMemoryEdmGenerator(namespace, MetadataConstants.CONTAINER_NAME, typeMapping, idPropName, eis, complexTypesInfo, myFunctions);
  }
}
