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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.impl.FeedGetMethodFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 10:24
 */
public class UrlResolverImpl implements UrlResolver {
  private static final Logger LOG = Logger.getInstance(UrlResolverImpl.class.getName());

  private final FeedClient myClient;
  private final FeedGetMethodFactory myMethods;

  public UrlResolverImpl(@NotNull final FeedClient client,
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
    HttpResponse execute = null;
    for(int _ = 100; _-->0;) {
      HttpGet ping = myMethods.createGet(feedUrl);
      ping.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);

      execute = myClient.execute(ping);

      String redirected = getRedirectedUrl(ping, execute);
      if (redirected != null) {
        LOG.debug("Redirected to " + redirected);
        feedUrl = redirected;
        EntityUtils.consume(execute.getEntity());
        continue;
      }

      final int statusCode = execute.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        EntityUtils.consume(execute.getEntity());
        throw new IOException("Failed to connect to " + feedUrl);
      }

      while (feedUrl.endsWith("/")) {
        feedUrl = feedUrl.substring(0, feedUrl.length() - 1);
      }
      return Pair.create(feedUrl, execute);
    }
    if (execute != null) {
      EntityUtils.consume(execute.getEntity());
    }
    throw new IOException("Failed to resolve redirects");
  }

  private HttpContext createContext() {
    return new BasicHttpContext();
  }

  @NotNull
  public RedirectStrategy getRedirectStrategy() {
    //TODO: could use http client for that
    return new DefaultRedirectStrategy();
  }

  @Nullable
  public String getRedirectedUrl(@NotNull HttpUriRequest request, @NotNull HttpResponse response) throws IOException {
    try {
      final RedirectStrategy redirectStrategy = getRedirectStrategy();
      if (!redirectStrategy.isRedirected(request,response, createContext())) {
        return null;
      }

      return redirectStrategy.getRedirect(request, response, createContext()).getURI().toString();
    } catch (ProtocolException e) {
      return null;
    }
  }
}
