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
import jetbrains.buildServer.version.ServerVersionHolder;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.net.ProxySelector;
import java.util.Arrays;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 16:24
 */
public class FeedHttpClientHolder extends HttpClientHolder implements FeedClient {
  public FeedHttpClientHolder() {
    super(createHttpClient());
  }

  private static DefaultHttpClient createHttpClient() {
    final String serverVersion = ServerVersionHolder.getVersion().getDisplayVersion();
    final HttpParams ps = new BasicHttpParams();

    DefaultHttpClient.setDefaultHttpParams(ps);
    HttpConnectionParams.setConnectionTimeout(ps, 300 * 1000);
    HttpConnectionParams.setSoTimeout(ps, 300 * 1000);
    HttpProtocolParams.setUserAgent(ps, "JetBrains TeamCity " + serverVersion);

    ps.setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, true);
    ps.setParameter(AuthPNames.CREDENTIAL_CHARSET, "utf-8");
    ps.setParameter(AuthPNames.TARGET_AUTH_PREF, Arrays.asList(AuthPolicy.BASIC));

    DefaultHttpClient httpclient = new DefaultHttpClient(new ThreadSafeClientConnManager(), ps);

    httpclient.setRoutePlanner(new ProxySelectorRoutePlanner(
            httpclient.getConnectionManager().getSchemeRegistry(),
            ProxySelector.getDefault()));
    httpclient.addRequestInterceptor(new RequestAcceptEncoding());
    httpclient.addResponseInterceptor(new ResponseContentEncoding());
    httpclient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, true));
    return httpclient;
  }

  public void dispose() {
    myClient.getConnectionManager().shutdown();
  }
}
