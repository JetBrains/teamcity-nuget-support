

package jetbrains.buildServer.nuget.server.trigger;

import java.util.*;
import java.util.stream.Collectors;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.nuget.feedReader.NuGetFeedCredentials;
import jetbrains.buildServer.nuget.server.TriggerUrlPostProcessor;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.tool.NuGetServerToolProvider;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequestFactory;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestModeFactory;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.tools.ServerToolManager;
import jetbrains.buildServer.tools.ToolVersionReference;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static jetbrains.buildServer.nuget.common.FeedConstants.PATH_TO_NUGET_EXE;
import static jetbrains.buildServer.nuget.common.NuGetServerConstants.NUGET_SERVER_CLI_PATH_WHITELIST_DEFAULT;
import static jetbrains.buildServer.nuget.common.NuGetServerConstants.NUGET_SERVER_CLI_PATH_WHITELIST_PROP;
import static jetbrains.buildServer.nuget.server.trigger.TriggerConstants.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 10.05.12 13:25
 */
public class TriggerRequestFactory {
  private final CheckRequestModeFactory myModeFactory;
  private final ServerToolManager myToolManager;
  private final PackageCheckRequestFactory myRequestFactory;
  @NotNull
  private final ExtensionHolder myExtensionHolder;

  public TriggerRequestFactory(@NotNull final CheckRequestModeFactory modeFactory,
                               @NotNull final ServerToolManager toolManager,
                               @NotNull final PackageCheckRequestFactory requestFactory,
                               @NotNull final ExtensionHolder extensionHolder) {
    myModeFactory = modeFactory;
    myToolManager = toolManager;
    myRequestFactory = requestFactory;
    myExtensionHolder = extensionHolder;
  }

  @NotNull
  public PackageCheckRequest createRequest(@NotNull PolledTriggerContext context) throws BuildTriggerException {
    final BuildTriggerDescriptor descriptor = context.getTriggerDescriptor();
    final Map<String, String> descriptorProperties = descriptor.getProperties();
    final String pkgId = descriptorProperties.get(PACKAGE);
    final String version = descriptorProperties.get(VERSION);
    final String username = descriptorProperties.get(USERNAME);
    final String password = descriptorProperties.get(PASSWORD);
    boolean isPrerelease = !StringUtil.isEmptyOrSpaces(descriptorProperties.get(INCLUDE_PRERELEASE));

    NuGetFeedCredentials credentials = null;
    if (username != null && password != null && !StringUtil.isEmptyOrSpaces(username) && !StringUtil.isEmptyOrSpaces(password)) {
      credentials = new NuGetFeedCredentials(username, password);
    }

    if (StringUtil.isEmptyOrSpaces(pkgId)) {
      throw new BuildTriggerException("The Package Id must be specified");
    }

    final String nugetVersionRef = descriptorProperties.get(TriggerConstants.NUGET_PATH_PARAM_NAME);
    if(StringUtil.isEmpty(nugetVersionRef)) {
      throw new BuildTriggerException("Trigger descriptor doesn't provide path to nuget.exe via parameter " + TriggerConstants.NUGET_PATH_PARAM_NAME);
    }
    final File nugetToolPathProvided = myToolManager.getUnpackedToolVersionPath(NuGetServerToolProvider.NUGET_TOOL_TYPE, nugetVersionRef, context.getBuildType().getProject());
    if(nugetToolPathProvided == null) {
      throw new BuildTriggerException("Failed to find NuGet.exe by tool reference: " + nugetVersionRef);
    }
    final File nugetPath = nugetToolPathProvided.isDirectory() ? new File(nugetToolPathProvided, PATH_TO_NUGET_EXE) : nugetToolPathProvided;
    if (!nugetPath.isFile()) {
      throw new BuildTriggerException("Failed to find NuGet.exe at: " + nugetPath);
    }

    if (!ToolVersionReference.isToolReference(nugetVersionRef)) {
      final String pathWhitelist = TeamCityProperties.getProperty(NUGET_SERVER_CLI_PATH_WHITELIST_PROP, NUGET_SERVER_CLI_PATH_WHITELIST_DEFAULT).toLowerCase();
      if (StringUtil.isNotEmpty(pathWhitelist)) {
        final Set<String> whitelist = new HashSet<String>(
          Arrays.stream(pathWhitelist.split(";"))
                .map(StringUtil::trim)
                .filter(StringUtil::isNotEmpty)
                .map(x -> FileUtil.normalizeSeparator(x))
                .collect(Collectors.toList()));
        if (!whitelist.contains(nugetPath.getPath().toLowerCase())) {
          throw new BuildTriggerException("Failed to run NuGet.exe at " + nugetPath + ". The custom NuGet path used by the trigger must be explicitly allowed on the server. Please review the trigger's settings.");
        }
      }
    }

    String source = descriptorProperties.get(SOURCE);
    if(StringUtils.isEmpty(source)){
      source = null;
    }
    else {
      for (TriggerUrlPostProcessor urlPostProcessor : myExtensionHolder.getExtensions(TriggerUrlPostProcessor.class)) {
        source = urlPostProcessor.updateTriggerUrl(context.getBuildType(), source);
      }
    }

    return myRequestFactory.createRequest(
            myModeFactory.createNuGetChecker(nugetPath),
            new SourcePackageReference(source, credentials, pkgId, version, isPrerelease));
  }
}
