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

package jetbrains.buildServer.nuget.feed.server.cache;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.*;
import jetbrains.buildServer.web.impl.TeamCityInternalKeys;
import jetbrains.buildServer.web.util.SessionUser;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 22.04.13 22:41
 */
public class ResponseCacheImpl implements ResponseCache {
  private static final Logger LOG = Logger.getInstance(ResponseCacheImpl.class.getName());
  private final Map<String, ResponseCacheEntry> myCache = new SoftValueHashMap<>();

  public ResponseCacheImpl(@NotNull EventDispatcher<BuildServerListener> dispatcher) {
    dispatcher.addListener(new BuildServerAdapter() {
      @Override
      public void cleanupFinished() {
        super.cleanupFinished();
        resetCache();
      }
    });
  }

  public void resetCache() {
    synchronized (myCache) {
      myCache.clear();
    }
  }

  @NotNull
  private String key(@NotNull final HttpServletRequest request) {
    StringBuilder builder = new StringBuilder();
    builder.append(request.getMethod());
    builder.append(" ");
    builder.append(request.getRequestURL());
    builder.append("@ '").append(WebUtil.getPathWithoutAuthenticationType(request));
    builder.append("?").append(WebUtil.createRequestParameters(request)).append("' ");

    final SUser user = SessionUser.getUser(request);
    if (user != null) {
      final String username = user.getUsername();
      builder.append(" as ").append(user.getId()).append(username != null ? username : "<null>");
    } else {
      builder.append("as no auth/user");
    }

    final String pageUrl = (String) request.getAttribute(TeamCityInternalKeys.PAGE_URL_KEY);
    if (!StringUtil.isEmpty(pageUrl)) {
      builder.append("URL: ").append(pageUrl);
    }

    return builder.toString();
  }

  @Override
  public void getOrCompute(@NotNull final HttpServletRequest request,
                           @NotNull final HttpServletResponse response,
                           @NotNull final ComputeAction action) throws Exception {
    final String key = key(request);
    final ResponseCacheEntry cached;
    synchronized (myCache) {
      cached = myCache.get(key);
    }

    if (cached != null) {
      cached.handleRequest(request, response);
      return;
    }

    LOG.debug("NuGet cache miss for: " + WebUtil.getRequestDump(request));
    final ResponseWrapper wrapped = new ResponseWrapper(response);

    action.compute(request, wrapped);

    final ResponseCacheEntry entry = wrapped.build();
    synchronized (myCache) {
      myCache.put(key, entry);
    }

    entry.handleRequest(request, response);
  }
}
