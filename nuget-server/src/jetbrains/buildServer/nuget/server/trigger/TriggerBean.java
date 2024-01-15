

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.agent.Constants;
import jetbrains.buildServer.nuget.common.FeedConstants;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.07.11 14:56
 */
public class TriggerBean {
  public String getNuGetToolTypeKey() {  return FeedConstants.NUGET_COMMANDLINE; }
  public String getNuGetExeKey() {  return TriggerConstants.NUGET_PATH_PARAM_NAME; }
  public String getSourceKey() {  return TriggerConstants.SOURCE;   }
  public String getPackageKey() {  return TriggerConstants.PACKAGE;   }
  public String getVersionKey() {  return TriggerConstants.VERSION;   }
  public String getPrereleaseKey() {  return TriggerConstants.INCLUDE_PRERELEASE;   }
  public String getUsername() { return TriggerConstants.USERNAME; }
  public String getPassword() { return TriggerConstants.PASSWORD; }
}
