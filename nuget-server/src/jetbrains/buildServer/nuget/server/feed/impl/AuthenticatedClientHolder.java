/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.feed.impl;

import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.FeedCredentials;
import org.apache.http.client.HttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 02.01.13 18:37
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class AuthenticatedClientHolder extends HttpClientHolder {
  private final FeedCredentials myCredentials;

  public AuthenticatedClientHolder(@NotNull HttpClient client,
                                   @NotNull FeedCredentials credentials) {
    super(client);
    myCredentials = credentials;
  }

  @NotNull
  @Override
  public FeedClient withCredentials(@Nullable FeedCredentials credentials) {
    throw new IllegalArgumentException("Credentials were set");
  }
}
