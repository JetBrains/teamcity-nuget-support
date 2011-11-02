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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import jetbrains.buildServer.nuget.server.feed.impl.FeedHttpClientHolder;
import jetbrains.buildServer.nuget.server.feed.server.process.NuGetServerPing;
import jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServerBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static jetbrains.buildServer.nuget.tests.integration.http.SimpleHttpServerBase.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 14:28
 */
public class NuGetServerPingIntegrationTest extends NuGetServerIntegrationTestBase {
  @Test
  public void testPing() {

    enableDebug();

    registerHttpHandler(new HttpServerHandler() {
      public SimpleHttpServerBase.Response processRequest(@NotNull String requestLine, @Nullable String path) {
        if (!(myHttpContextUrl + "/packages-ping.html").equals(path)) return null;

        if (checkContainsToken(requestLine)) {
          return createStringResponse(STATUS_LINE_200, Arrays.asList("Content-Type: text/plain; encoding=UTF-8", myTokens.getServerTokenHeaderName() + ": " + myTokens.getServerToken()), myTokens.getServerToken());
        } else {
          System.out.println("Failed to find authorization token in request!");
          return createStreamResponse(STATUS_LINE_500, Collections.<String>emptyList(), "invalid token".getBytes());
        }
      }
    });

    Assert.assertTrue(new NuGetServerPing(myNuGetServerAddresses, new FeedHttpClientHolder(), myTokens).pingNuGetServer());
  }
}
