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

package jetbrains.buildServer.nuget.server.feed.server.dotNetFeed;

import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 12:41
 */
public interface NuGetServerRunnerTokens {
  /**
   * @return http header that is used to provide auth token
   */
  @NotNull
  String getAccessTokenHeaderName();

  /**
   * Access token is used to authorize NuGet Feed server requests to TeamCity
   * to avoid leaks
   * @return unique token.
   */
  @NotNull
  String getAccessToken();


  @NotNull
  String getServerTokenHeaderName();

  @NotNull
  String getServerToken();
}
