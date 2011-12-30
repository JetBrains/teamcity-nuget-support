/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.server;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 19:29
 */
public class NuGetIndexEntry {
  @NotNull private final String myKey;
  @NotNull private final Map<String, String> myAttributes;
  @NotNull private final String myBuildTypeId;
  private final long myBuildId;

  public NuGetIndexEntry(@NotNull String key,
                         @NotNull Map<String, String> attributes,
                         @NotNull String buildTypeId,
                         long buildId) {
    myKey = key;
    myAttributes = attributes;
    myBuildTypeId = buildTypeId;
    myBuildId = buildId;
  }

  @NotNull
  public String getKey() {
    return myKey;
  }

  @NotNull
  public Map<String, String> getAttributes() {
    return myAttributes;
  }

  @NotNull
  public String getBuildTypeId() {
    return myBuildTypeId;
  }

  public long getBuildId() {
    return myBuildId;
  }
}
