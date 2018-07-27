/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.feed.server.odata4j.entity.PackageEntity;
import jetbrains.buildServer.nuget.feed.server.odata4j.functions.NuGetFeedFunction;
import jetbrains.buildServer.nuget.feed.server.odata4j.functions.NuGetFeedFunctions;
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes;
import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.core4j.Enumerable;
import org.core4j.Func1;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.producer.*;
import org.odata4j.producer.inmemory.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedInMemoryProducer extends InMemoryProducer {
  private static final Logger LOG = Logger.getInstance(NuGetFeedInMemoryProducer.class.getName());
  private final Object mySyncRoot = new Object();
  private final NuGetFeed myFeed;
  private final NuGetFeedFunctions myFunctions;
  private String myApiVersion;

  public NuGetFeedInMemoryProducer(@NotNull final NuGetFeed feed,
                                   @NotNull final NuGetFeedFunctions functions) {
    super(MetadataConstants.NUGET_GALLERY_NAMESPACE, NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE);
    myFeed = feed;
    myFunctions = functions;
    evaluation = new NuGetExpressionEvaluator();
  }

  public void register(Func1<RequestContext, Iterable<PackageEntity>> getFunc) {
    final Class<PackageEntity> entityClass = PackageEntity.class;
    final PropertyModel model1 = new BeanBasedPropertyModel(entityClass, true);
    final PropertyModel model2 = new EnumsAsStringsPropertyModelDelegate(model1);
    register(entityClass,
      model2,
      MetadataConstants.ENTITY_SET_NAME,
      NuGetAPIVersion.getVersionToUse() + MetadataConstants.ENTITY_TYPE_NAME,
      null,
      getFunc,
      PackageEntity.KeyPropertyNames);
  }

  @Override
  public EdmDataServices getMetadata() {
    final String apiVersionToUse = NuGetAPIVersion.getVersionToUse();
    synchronized (mySyncRoot) {
      if (!apiVersionToUse.equalsIgnoreCase(myApiVersion)) {
        cleanCachedMetadata();
        myApiVersion = apiVersionToUse;
      }
    }
    return super.getMetadata();
  }

  @Override
  public EntitiesResponse getEntities(ODataContext context, String entitySetName, QueryInfo queryInfo) {
    return super.getEntities(context, entitySetName, getQueryInfo(queryInfo));
  }

  @Override
  public BaseResponse callFunction(ODataContext context, EdmFunctionImport function, Map<String, OFunctionParameter> params, QueryInfo query, boolean isCountCall) {
    final NuGetFeedFunction targetFunction = myFunctions.find(function);
    if (targetFunction == null) {
      LOG.debug("Failed to process NuGet feed function call. Failed to find target function by name " + function.getName());
      throw new NotImplementedException();
    }

    final QueryInfo queryInfo = getQueryInfo(query);
    final Iterable<Object> functionCallResult = CollectionsUtil.convertCollection(
      targetFunction.call(function.getReturnType(), params, queryInfo), PackageEntityEx::new);
    if (functionCallResult == null) return null;

    final RequestContext rc = RequestContext.newBuilder(RequestContext.RequestType.GetEntities)
      .entitySetName(MetadataConstants.ENTITY_SET_NAME)
      .entitySet(getMetadata().getEdmEntitySet(MetadataConstants.ENTITY_SET_NAME))
      .queryInfo(queryInfo)
      .odataContext(context)
      .pathHelper(new PropertyPathHelper(queryInfo)).build();
    final InMemoryEntityInfo<?> ei = getEntityInfo(MetadataConstants.ENTITY_SET_NAME);

    if (isCountCall)
      return getCountResponse(rc, Enumerable.create(functionCallResult));
    else
      return getEntitiesResponse(rc, rc.getEntitySet(), Enumerable.create(functionCallResult), ei.getPropertyModel());
  }

  private QueryInfo getQueryInfo(QueryInfo query) {
    final String skipToken = query.skipToken;
    return QueryInfo.newBuilder()
      .setInlineCount(query.inlineCount)
      .setTop(query.top)
      .setSkip(query.skip)
      .setFilter(query.filter)
      .setOrderBy(query.orderBy)
      .setSkipToken(skipToken != null ? StringUtil.replace(skipToken, " ", "+") : null)
      .setCustomOptions(query.customOptions)
      .setExpand(query.expand)
      .setSelect(query.select)
      .build();
  }

  @Override
  protected InMemoryEdmGenerator newEdmGenerator(String namespace, InMemoryTypeMapping typeMapping, String idPropName, Map<String, InMemoryEntityInfo<?>> eis, Map<String, InMemoryComplexTypeInfo<?>> complexTypesInfo) {
    return new NuGetFeedInMemoryEdmGenerator(namespace, MetadataConstants.CONTAINER_NAME, typeMapping, idPropName, eis, complexTypesInfo, myFunctions);
  }

  @Override
  protected boolean comparePropertyValue(final String name, final Object v1, final Object v2) {
    if (NuGetPackageAttributes.VERSION.equals(name) || NuGetPackageAttributes.VERSION.equals(name)) {
      final SemanticVersion version1 = SemanticVersion.valueOf((String) v1);
      final SemanticVersion version2 = SemanticVersion.valueOf((String) v2);
      return !(version1 == null || version2 == null) && version1.compareTo(version2) == 0;
    }

    return super.comparePropertyValue(name, v1, v2);
  }

  @Override
  protected Object getEntityPojo(RequestContext rc) {
    final OEntityKey key = rc.getEntityKey();
    final Map<String, String> query = new HashMap<>();
    for (OProperty<?> property : key.asComplexProperties()) {
      query.put(property.getName(), (String) property.getValue());
    }

    final List<NuGetIndexEntry> result = myFeed.find(query, true);
    if (result.size() > 0) {
      return new PackageEntityEx(result.get(0));
    } else {
      return null;
    }
  }
}
