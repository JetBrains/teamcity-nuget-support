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
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.core4j.Func;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OEntity;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.producer.*;
import org.odata4j.producer.inmemory.*;

import java.util.List;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedInMemoryProducer extends InMemoryProducer {

  private final Logger LOG = Logger.getInstance(getClass().getName());
  private final NuGetFeedFunctions myFunctions;
  private final Object mySyncRoot = new Object();

  private String myApiVersion;

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
  public BaseResponse callFunction(ODataContext context, EdmFunctionImport function, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    final NuGetFeedFunction targetFunction = myFunctions.find(function);
    if(targetFunction == null){
      LOG.debug("Failed to process NuGet feed function call. Failed to find target function by name " + function.getName());
      throw new NotImplementedException();
    }

    final Iterable<Object> objects = targetFunction.call(function.getReturnType(), params, queryInfo);
    if(objects == null) return null;

    EdmEntitySet metadataEntitySet = getMetadata().findEdmEntitySet(function.getEntitySet().getName());
    final EdmEntitySet entitySet = metadataEntitySet != null ? metadataEntitySet : function.getEntitySet();

    final PropertyPathHelper pathHelper = new PropertyPathHelper(queryInfo);
    List<OEntity> entitiesList = CollectionsUtil.convertCollection(objects, new Converter<OEntity, Object>() {
      public OEntity createFrom(@NotNull Object source) {
        return toOEntity(entitySet, source, pathHelper);
      }
    });
    return Responses.entities(entitiesList, entitySet, null, null);
  }

  @Override
  protected InMemoryEdmGenerator newEdmGenerator(String namespace, InMemoryTypeMapping typeMapping, String idPropName, Map<String, InMemoryEntityInfo<?>> eis, Map<String, InMemoryComplexTypeInfo<?>> complexTypesInfo) {
    return new NuGetFeedInMemoryEdmGenerator(namespace, MetadataConstants.CONTAINER_NAME, typeMapping, idPropName, eis, complexTypesInfo, myFunctions);
  }
}
