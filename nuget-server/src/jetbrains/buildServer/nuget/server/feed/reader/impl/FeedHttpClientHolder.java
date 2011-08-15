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

import jetbrains.buildServer.version.ServerVersionHolder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 16:24
 */
public class FeedHttpClientHolder implements FeedClient {
  private final HttpClient myClient;

  public FeedHttpClientHolder() {
    final String serverVersion = ServerVersionHolder.getVersion().getDisplayVersion();

    HttpParams ps = new BasicHttpParams();
    DefaultHttpClient.setDefaultHttpParams(ps);

    HttpConnectionParams.setConnectionTimeout(ps, 10000);
    HttpConnectionParams.setSoTimeout(ps, 10000);
    HttpProtocolParams.setUserAgent(ps, "JetBrains TeamCity " + serverVersion);

    final DefaultHttpClient client = new ContentEncodingHttpClient(new ThreadSafeClientConnManager(), ps);
    client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true));

    myClient = client;
  }

  @NotNull
  public HttpResponse execute(@NotNull HttpUriRequest request) throws IOException {
    return myClient.execute(request);
  }

  public void dispose() {
    myClient.getConnectionManager().shutdown();
  }

}
