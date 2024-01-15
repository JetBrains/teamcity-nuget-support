

package jetbrains.buildServer.nuget.server.tool;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsManager;
import jetbrains.buildServer.nuget.server.settings.NuGetSettingsReader;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerResponsibility;
import jetbrains.buildServer.tools.*;
import jetbrains.buildServer.tools.installed.ToolPaths;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static jetbrains.buildServer.nuget.common.FeedConstants.NUGET_EXTENSION;

/**
 * Created by Evgeniy.Koshkin.
 */
public class NuGetServerToolPreProcessor extends ServerToolPreProcessorAdapter {
  private static final Logger LOG = Logger.getInstance(NuGetServerToolPreProcessor.class.getName());

  private static final String NUGET_TOOL_ID_PREFIX = "NuGet.CommandLine.";
  private static final String DEFAULT_NUGET_VERSION_SETTINGS_KEY = "default-nuget";
  private static final String JETBRAINS_NUGET = "jetbrains.nuget";
  private static final String NUPKG = "nupkg";
  private static final String AGENT = "agent";
  private static final String TOOLS = "tools";

  private final ServerPaths myServerPaths;
  private final ToolPaths myToolPaths;
  @NotNull
  private final NuGetSettingsManager myNugetSettings;
  @NotNull
  private final DefaultToolVersions myDefaultToolVersions;
  @NotNull
  private final ProjectManager myProjectManager;
  @NotNull
  private final ServerResponsibility myServerResponsibility;

  public NuGetServerToolPreProcessor(@NotNull final ServerPaths serverPaths,
                                     @NotNull final ToolPaths toolPaths,
                                     @NotNull final NuGetSettingsManager nugetSettings,
                                     @NotNull final DefaultToolVersions defaultToolVersions,
                                     @NotNull final ProjectManager projectManager,
                                     @NotNull ServerResponsibility myServerResponsibility) {
    myServerPaths = serverPaths;
    myToolPaths = toolPaths;
    myNugetSettings = nugetSettings;
    myDefaultToolVersions = defaultToolVersions;
    myProjectManager = projectManager;
    this.myServerResponsibility = myServerResponsibility;
  }

  @NotNull
  @Override
  public String getName() {
    return NuGetServerToolProvider.NUGET_TOOL_TYPE.getType();
  }

  @Override
  public void doBeforeServerStartup() throws ToolException {
    if (myServerResponsibility.canManageServerConfiguration()) {
      moveOldInstalledNugets();
    }
  }

  @Override
  public void doAfterServerStartup() throws ToolException {
    restoreDefaultVersion();
  }

  private void restoreDefaultVersion() {
    final ToolType nugetToolType = NuGetServerToolProvider.NUGET_TOOL_TYPE;
    final SProject rootProject = myProjectManager.getRootProject();
    if(myDefaultToolVersions.getDefaultVersionId(nugetToolType, rootProject) != null){
      LOG.debug("Default NuGet version is already set.");
      return;
    }
    final String defaultNuGetToolId = myNugetSettings.readSettings(NuGetSettingsComponent.NUGET, new NuGetSettingsManager.Func<NuGetSettingsReader, String>() {
      @Override
      public String executeAction(@NotNull NuGetSettingsReader action) {
        return action.getStringParameter(DEFAULT_NUGET_VERSION_SETTINGS_KEY);
      }
    });
    if(defaultNuGetToolId == null){
      LOG.debug("Can't restore default NuGet version since it is not set in NuGet settings file.");
      return;
    }

    LOG.debug("Default NuGet tool name read is " + defaultNuGetToolId);
    if(!defaultNuGetToolId.startsWith(NUGET_TOOL_ID_PREFIX)){
      LOG.debug("Default NuGet tool name is invalid, should start with " + NUGET_TOOL_ID_PREFIX);
      return;
    }

    final String defaultNuGetToolIdNormalized = StringUtils.removeEnd(defaultNuGetToolId, NUGET_EXTENSION);
    LOG.debug("Normalized default NuGet tool name is " + defaultNuGetToolIdNormalized);
    final String defaultNuGetVersion = defaultNuGetToolIdNormalized.substring(NUGET_TOOL_ID_PREFIX.length());
    LOG.debug("Default NuGet tool version resolved is " + defaultNuGetVersion);

    myDefaultToolVersions.setDefaultVersion(new SimpleInstalledToolVersion(new SimpleToolVersion(nugetToolType, defaultNuGetVersion, defaultNuGetToolIdNormalized), null, null, null), rootProject);
    LOG.debug("Succesfully restore NuGet default version " + defaultNuGetVersion);
  }

  private void moveOldInstalledNugets() throws ToolException {
    final File nugetPluginDataDir = new File(myServerPaths.getPluginDataDirectory(), JETBRAINS_NUGET);

    final File oldNuGetPackagesLocation = new File(nugetPluginDataDir, NUPKG);
    final File oldNuGetPackedToolsLocation = new File(nugetPluginDataDir, AGENT);
    final File oldNuGetUnPackedToolContentLocation = new File(nugetPluginDataDir, TOOLS);

    final File[] nupkgs = oldNuGetPackagesLocation.listFiles(FeedConstants.NUGET_TOOL_FILE_FILTER);
    if(nupkgs == null || nupkgs.length == 0){
      LOG.debug("No existing NuGet packages found on path " + oldNuGetPackagesLocation.getAbsolutePath());
    } else{
      for (final File oldLocation : nupkgs){
        final File newLocation = myToolPaths.getSharedToolPath(oldLocation);
        LOG.debug(String.format("Moving existing nupkg from %s to %s", oldLocation, newLocation));
        try {
          FileUtil.copy(oldLocation, newLocation);
          FileUtil.delete(oldLocation);
          LOG.debug(String.format("Succesfully moved nupkg from %s to %s", oldLocation, newLocation));
        } catch (IOException e) {
          throw new ToolException(String.format("Failed to move nupkg from %s to %s", oldLocation, newLocation), e);
        }
      }
    }

    FileUtil.delete(oldNuGetPackagesLocation);
    LOG.debug("Deleted directory " + oldNuGetPackagesLocation.getAbsolutePath());

    FileUtil.delete(oldNuGetPackedToolsLocation);
    LOG.debug("Deleted directory " + oldNuGetPackedToolsLocation.getAbsolutePath());

    FileUtil.delete(oldNuGetUnPackedToolContentLocation);
    LOG.debug("Deleted directory " + oldNuGetUnPackedToolContentLocation.getAbsolutePath());
  }
}
