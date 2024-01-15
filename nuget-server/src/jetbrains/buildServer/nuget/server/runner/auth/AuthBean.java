

package jetbrains.buildServer.nuget.server.runner.auth;

import jetbrains.buildServer.nuget.common.PackagesConstants;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 04.01.13 11:10
 */
public class AuthBean {
  public String getFeedKey() { return PackagesConstants.NUGET_AUTH_FEED; }
  public String getUsernameKey() { return PackagesConstants.NUGET_AUTH_USERNAME; }
  public String getPasswordKey() { return PackagesConstants.NUGET_AUTH_PASSWORD; }
}
