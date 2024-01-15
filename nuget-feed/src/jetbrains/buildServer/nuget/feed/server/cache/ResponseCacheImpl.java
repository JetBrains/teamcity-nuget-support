

package jetbrains.buildServer.nuget.feed.server.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.*;
import jetbrains.buildServer.web.impl.TeamCityInternalKeys;
import jetbrains.buildServer.web.util.SessionUser;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 22.04.13 22:41
 */
public class ResponseCacheImpl implements ResponseCache {
  private static final Logger LOG = Logger.getInstance(ResponseCacheImpl.class.getName());
  private final Cache<String, ResponseCacheEntry> myCache;

  public ResponseCacheImpl(@NotNull EventDispatcher<BuildServerListener> dispatcher) {
    myCache = Caffeine.newBuilder()
      .executor(Runnable::run)
      .maximumSize(TeamCityProperties.getLong(NuGetFeedConstants.PROP_NUGET_FEED_CACHE_SIZE, 100))
      .build();

    dispatcher.addListener(new BuildServerAdapter() {
      @Override
      public void cleanupFinished() {
        resetCache();
      }
    });
  }

  public void resetCache() {
    try {
      LOG.debug("Resetting packages cache");
      myCache.invalidateAll();
    } finally {
      LOG.debug("Resetted packages cache");
    }
  }

  @NotNull
  private String key(@NotNull final String feedName,
                     @NotNull final HttpServletRequest request) {
    StringBuilder builder = new StringBuilder();
    builder.append(feedName);
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
  public void getOrCompute(@NotNull final NuGetFeedData feedData,
                           @NotNull final HttpServletRequest request,
                           @NotNull final HttpServletResponse response,
                           @NotNull final ComputeAction action) throws Exception {
    final String key = key(feedData.getKey(), request);
    final ResponseCacheEntry entry = myCache.get(key, s -> {
      LOG.debug("NuGet cache miss for: " + WebUtil.getRequestDump(request));
      try {
        final ResponseWrapper wrapped = new ResponseWrapper(response);
        action.compute(feedData, request, wrapped);
        return wrapped.build();
      } catch (Exception e) {
        LOG.warnAndDebugDetails("Failed to process request", e);
        return null;
      }
    });

    if (entry != null) {
      entry.handleRequest(request, response);
    } else {
      action.compute(feedData, request, response);
    }
  }
}
