

package jetbrains.buildServer.nuget.agent.parameters.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.Constants;
import jetbrains.buildServer.nuget.agent.parameters.PackageSourceManager;
import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created 04.01.13 19:19
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class PackageSourceManagerImpl implements PackageSourceManager {

  private static final Logger LOG = Logger.getInstance(PackageSourceManagerImpl.class.getName());

  @NotNull
  public Set<PackageSource> getGlobalPackageSources(@NotNull AgentRunningBuild build) {
    final Map<String, PackageSource> result = new HashMap<String, PackageSource>();

    for (AgentBuildFeature feature : build.getBuildFeaturesOfType(PackagesConstants.AUTH_FEATURE_TYPE)) {
      final String feed = feature.getParameters().get(PackagesConstants.NUGET_AUTH_FEED);
      final String user = feature.getParameters().get(PackagesConstants.NUGET_AUTH_USERNAME);
      final String pass = feature.getParameters().get(PackagesConstants.NUGET_AUTH_PASSWORD);

      result.put(normalizeUrl(feed), source(feed, user, pass));
    }

    final Map<String, String> parameters = build.getSharedBuildParameters().getSystemProperties();
    for (String key : parameters.keySet()) {
      if (NuGetServerConstants.FEED_PARAM_AUTH_PATTERN.matcher(Constants.SYSTEM_PREFIX + key).find()) {
        final String feedUrl = parameters.get(key);
        if (StringUtil.isEmptyOrSpaces(feedUrl)) {
          LOG.debug("Failed to resolve TeamCity NuGet feed url via config parameter " + key);
        } else {
          result.put(normalizeUrl(feedUrl), source(feedUrl, build.getAccessUser(), build.getAccessCode()));
        }
      }
    }

    return new HashSet<PackageSource>(result.values());
  }

  @NotNull
  private String normalizeUrl(@NotNull String url) {
    try {
      return URI.create(url).normalize().toString();
    } catch (Exception e) {
      return url; // probably not URL, use as is
    }
  }

  @NotNull
  private PackageSource source(@NotNull final String feed,
                               @Nullable final String user,
                               @Nullable final String pass) {
    return new PackageSource() {
      @NotNull
      public String getSource() {
        return feed;
      }

      @Nullable
      public String getUsername() {
        return user;
      }

      @Nullable
      public String getPassword() {
        return pass;
      }

      @Override
      public int hashCode() {
        return feed.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PackageSource)) return false;
        PackageSource that = (PackageSource) obj;
        return feed.equals(that.getSource());
      }
    };
  }
}
