

package jetbrains.buildServer.nuget.server.runner.publish;

import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 21.07.11 13:51
 */
public class PublishBean {
  public String getNuGetPathKey() { return PackagesConstants.NUGET_PATH; }
  public String getNuGetSourceKey() { return PackagesConstants.NUGET_PUBLISH_SOURCE; }
  public String getApiKey() { return PackagesConstants.NUGET_API_KEY; }
  public String getNuGetPublishFilesKey() {return PackagesConstants.NUGET_PUBLISH_FILES; }
  public String getPushCustomCommandline() { return PackagesConstants.NUGET_PUSH_CUSTOM_COMMANDLINE; }
  public String getNugetToolTypeName() {return FeedConstants.NUGET_COMMANDLINE;}
  public String getNuGetFeedApiKeyReference() { return ReferencesResolverUtil.makeReference(NuGetServerConstants.FEED_REFERENCE_AGENT_API_KEY_PROVIDED); }
}
