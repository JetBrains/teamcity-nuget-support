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

package jetbrains.buildServer.nuget.server.feed.server.dotNetFeed.process;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerTokens;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerUri;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 24.10.11 19:07
 */
public class NuGetServerPingCommandImpl implements NuGetServerPingCommand {
  private static final Logger LOG = Logger.getInstance(NuGetServerPingCommandImpl.class.getName());

  @NotNull private final NuGetServerUri myUri;
  @NotNull private final FeedClient myHttp;
  @NotNull private final NuGetServerRunnerTokens mySettings;

  public NuGetServerPingCommandImpl(@NotNull final NuGetServerUri uri,
                                    @NotNull final FeedClient http,
                                    @NotNull final NuGetServerRunnerTokens settings) {
    myUri = uri;
    myHttp = http;
    mySettings = settings;
  }

  public boolean pingNuGetServer() {
    final String uri = myUri.getNuGetPingUri();
    if (uri == null) return true;
    final HttpGet get = new HttpGet(uri);
    try {
      final HttpResponse execute = myHttp.execute(get);
      final StatusLine line = execute.getStatusLine();

      if (LOG.isDebugEnabled()) {
        final HttpEntity entity = execute.getEntity();
        if (entity != null) {
          final ByteArrayOutputStream bos = new ByteArrayOutputStream();
          entity.writeTo(bos);
          LOG.debug("Ping outpout: " + bos.toString());
        }
        LOG.debug("NuGet Server HTTP response status " + line.toString() + " was returned from NuGet Server");
      }

      if (line.getStatusCode() != HttpStatus.SC_OK) {
        LOG.warn("NuGet Server HTTP response error status '" + line.toString() + "' was returned");
        return false;
      }

      final Header[] hostId = execute.getHeaders(mySettings.getServerTokenHeaderName());
      if (hostId == null || hostId.length != 1 || !mySettings.getServerToken().equals(hostId[0].getValue())) {
        LOG.warn("NuGet server failed to ping TeamCity server. Response token does not match. Check TeamCity server url that is used for NuGet Server in TeamCity");
        return false;
      }

      return true;
    } catch(Throwable t) {
      LOG.warn("Failed to ping NuGet Server process. " + t.getMessage());
      LOG.debug("Failed to ping NuGet Server process. " + t.getMessage(), t);
      return false;
    } finally {
      get.abort();
    }
  }
}
