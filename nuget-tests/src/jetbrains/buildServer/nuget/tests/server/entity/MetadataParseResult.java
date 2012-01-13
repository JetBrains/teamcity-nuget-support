/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.server.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 07.01.12 9:49
*/
public final class MetadataParseResult {
  private final Collection<MetadataBeanProperty> myKey;
  private final Collection<MetadataBeanProperty> myData;

  public MetadataParseResult(@NotNull final Collection<MetadataBeanProperty> key,
                             @NotNull final Collection<MetadataBeanProperty> data) {
    myKey = key;
    myData = data;
  }

  @NotNull
  public Collection<MetadataBeanProperty> getKey() {
    return myKey;
  }

  @NotNull
  public Collection<MetadataBeanProperty> getData() {
    return myData;
  }
}
