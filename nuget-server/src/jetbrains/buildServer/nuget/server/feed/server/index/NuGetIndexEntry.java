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

package jetbrains.buildServer.nuget.server.feed.server.index;

import jetbrains.buildServer.nuget.common.PackageInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 19:29
 */
public class NuGetIndexEntry {
  @NotNull private final String myKey;
  @NotNull private final Map<String, String> myAttributes;

  public NuGetIndexEntry(@NotNull String key,
                         @NotNull Map<String, String> attributes) {
    myKey = key;
    myAttributes = attributes;
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
  public String getPackageDownloadUrl() {
    return myAttributes.get(PackagesIndex.TEAMCITY_DOWNLOAD_URL);
  }

  @NotNull
  public PackageInfo getPackageInfo() {
    return new PackageInfo(myAttributes.get("Id"), myAttributes.get("Version"));
  }

  @Override
  public String toString() {
    return "NuGetIndexEntry{" +
            "myKey='" + myKey + '\'' +
            '}';
  }
}
