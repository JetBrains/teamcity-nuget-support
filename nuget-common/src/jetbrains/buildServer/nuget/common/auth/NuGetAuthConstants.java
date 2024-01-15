

package jetbrains.buildServer.nuget.common.auth;

/**
 * Created by Evgeniy.Koshkin on 17.12.2015
 */
public class NuGetAuthConstants {
  public static final String TEAMCITY_NUGET_FEEDS_ENV_VAR = "TEAMCITY_NUGET_FEEDS";
  public static final String NUGET_CREDENTIALPROVIDERS_PATH_ENV_VAR = "NUGET_CREDENTIALPROVIDERS_PATH";
  public static final String NUGET_PLUGIN_PATH_ENV_VAR = "NUGET_PLUGIN_PATHS";
  public static final String NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS_ENV_VAR = "NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS";
  public static final String NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS_ENV_VAR = "NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS";

  public static final int NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS = 30;
  public static final int NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS = 30;
}
