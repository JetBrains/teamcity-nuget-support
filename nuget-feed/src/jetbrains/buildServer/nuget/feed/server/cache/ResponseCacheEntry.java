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

package jetbrains.buildServer.nuget.feed.server.cache;

import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 22.04.13 22:34
 */
public class ResponseCacheEntry {
  private final Map<String, String> myHeaders = new TreeMap<String, String>();
  private final byte[] myGZippedContent;
  private final int myStatus;

  public ResponseCacheEntry(@NotNull final Map<String, String> headers,
                            @NotNull final byte[] GZippedContent,
                            final int status) {
    myHeaders.putAll(headers);
    myGZippedContent = GZippedContent;
    myStatus = status;
  }

  public void handleRequest(@NotNull final HttpServletRequest request,
                            @NotNull final HttpServletResponse response) throws Exception {
    for (Map.Entry<String, String> e : myHeaders.entrySet()) {
      response.setHeader(e.getKey(), e.getValue());
    }
    response.setHeader("Content-Encoding", "gzip");
    response.setStatus(myStatus);
    ServletOutputStream stream = response.getOutputStream();
    stream.write(myGZippedContent);
    stream.flush();
  }
}
