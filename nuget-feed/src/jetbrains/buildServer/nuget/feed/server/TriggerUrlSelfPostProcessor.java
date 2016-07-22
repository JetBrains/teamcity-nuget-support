/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.feed.server;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.nuget.server.TriggerUrlPostProcessor;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created 26.06.13 19:09
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class TriggerUrlSelfPostProcessor implements TriggerUrlPostProcessor {
  private final NuGetServerPropertiesProvider myProvider;

  public TriggerUrlSelfPostProcessor(@NotNull NuGetServerPropertiesProvider provider) {
    myProvider = provider;
  }

  @NotNull
  public String updateTriggerUrl(@NotNull BuildTriggerDescriptor context, @NotNull String url) {
    if (!ReferencesResolverUtil.mayContainReference(url)) return url;

    final Map<String,String> map = myProvider.getProperties();
    for (Map.Entry<String, String> e : map.entrySet()) {
      url = url.replace(ReferencesResolverUtil.makeReference(e.getKey()), e.getValue());
    }
    return url;
  }
}
