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

package jetbrains.buildServer.nuget.server.feedReader;

import jetbrains.buildServer.nuget.feedReader.NuGetFeedClient;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedCredentials;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetFeedHttpClientHolder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Created by Evgeniy.Koshkin on 11-Mar-16.
 */
public class NuGetFeedClientHolder implements NuGetFeedClient {
  private final NuGetFeedHttpClientHolder myFeedClient = new NuGetFeedHttpClientHolder();

  @NotNull
  @Override
  public HttpResponse execute(@NotNull HttpUriRequest httpUriRequest) throws IOException {
    return myFeedClient.execute(httpUriRequest);
  }

  @NotNull
  @Override
  public NuGetFeedClient withCredentials(@Nullable NuGetFeedCredentials nuGetFeedCredentials) {
    return myFeedClient.withCredentials(nuGetFeedCredentials);
  }

  @Override
  public boolean hasCredentials() {
    return myFeedClient.hasCredentials();
  }

  public void dispose() {
    myFeedClient.dispose();
  }
}
