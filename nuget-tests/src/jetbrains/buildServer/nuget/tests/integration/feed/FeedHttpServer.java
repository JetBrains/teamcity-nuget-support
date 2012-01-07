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

package jetbrains.buildServer.nuget.tests.integration.feed;

import jetbrains.buildServer.nuget.server.feed.FeedServer;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 10.09.11 22:41
*/
public class FeedHttpServer extends SimpleHttpServerBase {
  private final String myBaseUrl;
  private final FeedServer myServer;

  public FeedHttpServer(@NotNull final String baseUrl,
                        @NotNull final FeedServer server) {
    myBaseUrl = baseUrl;
    myServer = server;
  }

  private Map<String, String> parseArguments(@Nullable String query) throws UnsupportedEncodingException {
    if (query == null || StringUtil.isEmptyOrSpaces(query)) return Collections.emptyMap();

    final String[] split = query.split("&");
    final Map<String, String> map = new TreeMap<String, String>();
    for (String s : split) {
      String[] kv = s.split("=");
      if (kv.length != 2) continue;
      //TODO: decode
      final String key = URLDecoder.decode(kv[0], "utf-8");
      final String value = URLDecoder.decode(kv[1], "utf-8");
      map.put(key, value);
    }
    System.out.println("HTTP params: " + map);
    return map;
  }

  @Override
  protected Response getResponse(String requestString) {
    final String requestPath = getRequestPath(requestString);
    System.out.println("\r\n\r\n");
    System.out.println("HTTP request: " + requestString);
    System.out.println("HTTP path: " + requestPath);

    if (requestPath == null || !requestPath.startsWith(myBaseUrl)) {
      return createStreamResponse(STATUS_LINE_404, Collections.<String>emptyList(), new byte[0]);
    }
    final String[] split = requestPath.substring(myBaseUrl.length()).split("\\?", 2);
    final String path = split[0];
    System.out.println("Mapped path: " + path);

    try {
      Writer w = new StringWriter();
      Map<String, String> argz = parseArguments(split.length > 1 ? split[1] : null);
      myServer.handleRequest(path, argz, w);
      return createStreamResponse(STATUS_LINE_200, Collections.<String>emptyList(), w.toString().getBytes());
    } catch (IOException e) {
      e.printStackTrace();
      return createStreamResponse(STATUS_LINE_500, Collections.<String>emptyList(), e.toString().getBytes());
    }
  }
}
