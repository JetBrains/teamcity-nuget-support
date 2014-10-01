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

import com.google.common.collect.Lists;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.edm.EdmFunctionImport;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetFeedFunctions {
  private final Collection<NuGetFeedFunction> myAPIv2Functions;

  public NuGetFeedFunctions(@NotNull PackagesIndex index) {
    myAPIv2Functions = Lists.newArrayList(new FindPackagesByIdFunction(index) , new GetUpdatesFunction(index) /*, new SearchFunction()*/);
  }

  @Nullable
  public NuGetFeedFunction find(@NotNull final EdmFunctionImport name) {
    return CollectionsUtil.findFirst(myAPIv2Functions, new Filter<NuGetFeedFunction>() {
      public boolean accept(@NotNull NuGetFeedFunction data) {
        return data.getName().equals(name.getName());
      }
    });
  }

  public Iterable<? extends NuGetFeedFunction> getAll() {
    return Collections.unmodifiableCollection(myAPIv2Functions);
  }
}
