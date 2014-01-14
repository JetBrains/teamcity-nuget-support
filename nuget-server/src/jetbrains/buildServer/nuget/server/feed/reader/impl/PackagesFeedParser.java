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

package jetbrains.buildServer.nuget.server.feed.reader.impl;

import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 15:14
 */
public interface PackagesFeedParser {
  /**
   * Parses the feed output
   * @param root Xml tree of ATOM resoponse
   * @param results collection to add results
   * @return url of next portion af any or null
   */
  @Nullable
  String readPackages(@NotNull Element root, @NotNull Collection<FeedPackage> results);
}
