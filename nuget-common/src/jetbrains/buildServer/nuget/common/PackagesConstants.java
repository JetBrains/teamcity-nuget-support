

package jetbrains.buildServer.nuget.common;

import jetbrains.buildServer.ArtifactsConstants;
import jetbrains.buildServer.agent.Constants;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.07.11 13:56
 */
public interface PackagesConstants {
  public static final String INSTALL_RUN_TYPE = "jb.nuget.installer"; //no more than 30 chars
  public static final String PUBLISH_RUN_TYPE = "jb.nuget.publish"; //run-type could never exceed 30 chars
  public static final String PACK_RUN_TYPE = "jb.nuget.pack"; //run-type could never exceed 30 chars
  public static final String AUTH_FEATURE_TYPE = "jb.nuget.auth"; //run-type could never exceed 30 chars

  public static final String[] ALL_NUGET_RUN_TYPES = {INSTALL_RUN_TYPE, PUBLISH_RUN_TYPE, PACK_RUN_TYPE};

  public static final String NUGET_PATH = "nuget.path";
  public static final String NUGET_SOURCES = "nuget.sources";
  public static final String NUGET_EXCLUDE_VERSION = "nuget.excludeVersion";

  public static final String NUGET_USE_RESTORE_COMMAND = "nuget.use.restore";
  public static final String NUGET_USE_RESTORE_COMMAND_RESTORE_MODE = "restore";
  public static final String NUGET_USE_RESTORE_COMMAND_INSTALL_MODE = "install";

  public static final String NUGET_NO_CACHE = "nuget.noCache";
  public static final String NUGET_RESTORE_CUSOM_COMMANDLINE= "nuget.restore.commandline";

  public static final String NUGET_UPDATE_PACKAGES = "nuget.updatePackages";
  public static final String NUGET_UPDATE_PACKAGES_SAFE = "nuget.updatePackages.safe";
  public static final String NUGET_UPDATE_PACKAGES_PRERELEASE = "nuget.updatePackages.include.prerelease";
  public static final String NUGET_UPDATE_PACKAGE_IDS = "nuget.updatePackages.ids";
  public static final String NUGET_UPDATE_MODE = "nuget.updatePackages.mode";
  public static final String NUGET_UPDATE_CUSOM_COMMANDLINE= "nuget.update.commandline";

  public static final String SLN_PATH = "sln.path";

  public static final String NUGET_USED_PACKAGES_DIR = ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR + "/nuget";
  public static final String NUGET_USED_PACKAGES_FILE = "nuget.xml";

  public static final String NUGET_PUBLISH_FILES = "nuget.publish.files";
  public static final String NUGET_PUBLISH_SOURCE = "nuget.publish.source";
  public static final String NUGET_PUSH_CUSTOM_COMMANDLINE= "nuget.push.commandline";
  public static final String NUGET_API_KEY = Constants.SECURE_PROPERTY_PREFIX + "nuget.api.key";

  public static final String NUGET_PACK_OUTPUT_CLEAR = "nuget.pack.output.clean";
  public static final String NUGET_PACK_OUTPUT_DIR = "nuget.pack.output.directory";
  public static final String NUGET_PACK_PUBLISH_ARTIFACT = "nuget.pack.as.artifact";
  public static final String NUGET_PACK_PREFER_PROJECT = "nuget.pack.prefer.project";
  public static final String NUGET_PACK_BASE_DIRECTORY_MODE = "nuget.pack.project.dir";
  public static final String NUGET_PACK_BASE_DIR = "nuget.pack.base.directory";
  public static final String NUGET_PACK_VERSION = "nuget.pack.version";
  public static final String NUGET_PACK_SPEC_FILE = "nuget.pack.specFile";
  public static final String NUGET_PACK_EXCLUDE_FILES = "nuget.pack.excludes";
  public static final String NUGET_PACK_PROPERTIES = "nuget.pack.properties";
  public static final String NUGET_PACK_CUSOM_COMMANDLINE= "nuget.pack.commandline";
  public static final String NUGET_PACK_AS_TOOL= "nuget.pack.pack.mode.tool";
  public static final String NUGET_PACK_INCLUDE_SOURCES = "nuget.pack.include.sources" ;

  public static final String NUGET_AUTH_FEED = "nuget.auth.feed";
  public static final String NUGET_AUTH_USERNAME = "nuget.auth.username";
  public static final String NUGET_AUTH_PASSWORD = Constants.SECURE_PROPERTY_PREFIX + "nuget.auth.password";
}
