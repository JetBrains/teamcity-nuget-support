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

package jetbrains.buildServer.nuget.server.feed.impl;

import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.FeedCredentials;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.BasicHttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

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

  @Override
  public boolean hasCredentials() {
    return true;
  }

  @NotNull
  @Override
  public FeedClient withCredentials(@Nullable FeedCredentials credentials) {
    throw new IllegalArgumentException("Credentials were set");
  }

  @NotNull
  @Override
  public HttpResponse execute(@NotNull HttpUriRequest request) throws IOException {
    final BasicHttpContext ctx = new BasicHttpContext();

    //TODO: consider redirects
    final HttpHost host = URIUtils.extractHost(request.getURI());

    // Create AuthCache instance
    final AuthCache authCache = new BasicAuthCache();
    // Generate BASIC scheme object and add it to the local auth cache
    final BasicScheme basicAuth = new BasicScheme();
    authCache.put(host, basicAuth);
    // Add AuthCache to the execution context

    ctx.setAttribute(ClientContext.AUTH_CACHE, authCache);

    final CredentialsProvider cred = new BasicCredentialsProvider();
    cred.setCredentials(new AuthScope(host.getHostName(), host.getPort()), new UsernamePasswordCredentials(myCredentials.getUsername(), myCredentials.getPassword()));
    ctx.setAttribute(ClientContext.CREDS_PROVIDER, cred);

    return myClient.execute(request, ctx);
  }
}
