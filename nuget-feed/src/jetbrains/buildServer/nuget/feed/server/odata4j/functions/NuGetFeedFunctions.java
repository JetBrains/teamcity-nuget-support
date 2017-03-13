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

package jetbrains.buildServer.nuget.feed.server.odata4j.functions;

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.edm.EdmFunctionImport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedFunctions {
  private final Map<String, NuGetFeedFunction> myAPIv2Functions = new HashMap<>();

  public NuGetFeedFunctions(@NotNull NuGetFeed feed) {
    addFunction(new FindPackagesByIdFunction(feed));
    addFunction(new GetUpdatesFunction(feed));
    addFunction(new SearchFunction(feed));
  }

  private void addFunction(NuGetFeedFunction feedFunction) {
    myAPIv2Functions.put(feedFunction.getName(), feedFunction);
  }

  @Nullable
  public NuGetFeedFunction find(@NotNull final EdmFunctionImport name) {
    return myAPIv2Functions.get(name.getName());
  }

  public Iterable<? extends NuGetFeedFunction> getAll() {
    return Collections.unmodifiableCollection(myAPIv2Functions.values());
  }
}
