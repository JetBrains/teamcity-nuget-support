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

package jetbrains.buildServer.nuget.server.feed.reader.impl;

import com.intellij.openapi.util.Pair;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 10:24
 */
public class UrlResolver {
  private final FeedClient myClient;
  private final FeedGetMethodFactory myMethods;

  public UrlResolver(@NotNull final FeedClient client,
                     @NotNull final FeedGetMethodFactory methods) {
    myClient = client;
    myMethods = methods;
  }

  /**
   * Generates GET request to a given URL.
   * If HTTP Status 3xx is returned, Location header is
   * used to generate next request unless non 3xx status
   * is returned
   * @param feedUrl url to ping and resolve
   * @return resolved URL and HttpResponse
   * @throws IOException if failed to communicate or non 3xx or 200 status returned
   */
  @NotNull
  public Pair<String, HttpResponse> resolvePath(@NotNull String feedUrl) throws IOException {
    for(int _ = 100; _-->0;) {
      HttpGet ping = myMethods.createGet(feedUrl);
      ping.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

      final HttpResponse execute = myClient.getClient().execute(ping);
      final int statusCode = execute.getStatusLine().getStatusCode();
      if (statusCode / 100 == 3) {
        final Header location = execute.getFirstHeader("Location");
        if (location != null) {
          feedUrl = location.getValue();
          continue;
        }
      }

      if (statusCode != HttpStatus.SC_OK) {
        throw new IOException("Failed to connect to " + feedUrl);
      }
      return Pair.create(feedUrl, execute);
    }
    throw new IOException("Failed to resolve redirects");
  }

}
