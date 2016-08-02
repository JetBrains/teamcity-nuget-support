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

import jetbrains.buildServer.nuget.feed.server.MetadataConstants;
import org.jetbrains.annotations.NotNull;
import org.odata4j.edm.EdmEntitySet;

/**
 * @author Evgeniy.Koshkin
 */
public class PackagesEntitySet {
  @NotNull
  private static final EdmEntitySet.Builder myPackagesEntitySetBuilder
          = new EdmEntitySet.Builder().setName(MetadataConstants.ENTITY_SET_NAME);

  public static EdmEntitySet.Builder getBuilder() {
    return myPackagesEntitySetBuilder;
  }
}
