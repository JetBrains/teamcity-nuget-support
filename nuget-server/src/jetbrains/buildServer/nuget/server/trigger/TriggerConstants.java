

package jetbrains.buildServer.nuget.server.trigger;

import jetbrains.buildServer.agent.Constants;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.07.11 15:00
 */
public interface TriggerConstants {
  String TRIGGER_ID = "nuget.simple";
  String NUGET_PATH_PARAM_NAME = "nuget.exe";

  String SOURCE = "nuget.source";
  String PACKAGE = "nuget.package";
  String VERSION = "nuget.version";
  String INCLUDE_PRERELEASE = "nuget.include.prerelease";
  String USERNAME = "nuget.username";
  String PASSWORD = Constants.SECURE_PROPERTY_PREFIX + "nuget.password";
}
