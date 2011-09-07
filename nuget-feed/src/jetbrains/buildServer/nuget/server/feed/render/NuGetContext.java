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

package jetbrains.buildServer.nuget.server.feed.render;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.09.11 23:59
 */
public class NuGetContext {
  public String getBaseUri() {
    return "http://packages.nuget.org/v1/FeedService.svc/";
  }

  public String getFeedId() {
    return getBaseUri() + getTitle();
  }

  public Date getUpdated() {
    return new Date();
  }

  public String getTitle() {
    return "Packages";
  }

  public String getEncoding() {
    return "utf-8";
  }

  public String resolveUrl(String downloadPath) {
    return getBaseUri() + downloadPath;
  }

  public String createId(@NotNull final String itemName, @NotNull final String itemVersion) {
    return getBaseUri() + "Packages(id='" + itemName + "',Version='" + itemVersion + "')";
  }
}
