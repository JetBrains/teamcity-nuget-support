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

import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import jetbrains.buildServer.nuget.feed.server.MetadataConstants;
import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed;
import jetbrains.buildServer.nuget.feed.server.odata4j.functions.NuGetFeedFunctions;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.odata4j.producer.ODataProducer;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.xppimpl.XmlPullXMLFactoryProvider2;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 19:09
 */
public class NuGetProducerHolder {
  private final NuGetFeedInMemoryProducer myProducer;
  private final SemanticVersion VERSION_20 = Objects.requireNonNull(SemanticVersion.valueOf("2.0.0"));

  public NuGetProducerHolder(@NotNull final NuGetFeed feed, @NotNull final NuGetAPIVersion apiVersion) {
    final NuGetFeedFunctions functions = new NuGetFeedFunctions(feed);
    //Workaround for Xml generation. Default STAX xml writer
    //used to generate <foo></foo> that is badly parsed in
    //.NET OData WCF client
    XMLFactoryProvider2.setInstance(new XmlPullXMLFactoryProvider2());
    myProducer = new NuGetFeedInMemoryProducer(feed, functions, apiVersion);
    myProducer.register((context) -> {
      boolean includeSemVer2 = includeSemVer2(context.getQueryInfo().customOptions);
      return CollectionsUtil.convertCollection(feed.getAll(includeSemVer2), PackageEntityEx::new);
    });
  }

  @NotNull
  public ODataProducer getProducer() {
    return myProducer;
  }

  private boolean includeSemVer2(final Map<String, String> params) {
    String semVerLevel = params.get(MetadataConstants.SEMANTIC_VERSION);
    if (semVerLevel != null) {
      semVerLevel = StringUtil.trimEnd(StringUtil.trimStart(semVerLevel, "'"), "'");
      final SemanticVersion version = SemanticVersion.valueOf(semVerLevel);
      return version != null && version.compareTo(VERSION_20) >= 0;
    }
    return false;
  }
}
