

package jetbrains.buildServer.nuget.server.tool.impl;

import jetbrains.buildServer.nuget.server.tool.NuGetServerToolProvider;
import jetbrains.buildServer.tools.ToolType;
import jetbrains.buildServer.tools.available.DownloadableToolVersion;
import org.jetbrains.annotations.NotNull;

/**
 * @author Evgeniy.Koshkin
 */
public class DownloadableNuGetTool implements DownloadableToolVersion {
  private final String myId;
  @NotNull
  private final String myVersion;
  @NotNull
  private final String myDownloadUrl;
  @NotNull
  private final String myDestinationFileName;

  public DownloadableNuGetTool(@NotNull String version, @NotNull String downloadUrl, @NotNull String destinationFileName) {
    myVersion = version;
    myDownloadUrl = downloadUrl;
    myDestinationFileName = destinationFileName;
    myId = NuGetServerToolProvider.NUGET_TOOL_TYPE.getType() + "." + version;
  }

  @NotNull
  public String getId(){
    return myId;
  }

  @NotNull
  @Override
  public String getDownloadUrl() {
    return myDownloadUrl;
  }

  @NotNull
  @Override
  public String getDestinationFileName() {
    return myDestinationFileName;
  }

  @NotNull
  @Override
  public ToolType getType() {
    return NuGetServerToolProvider.NUGET_TOOL_TYPE;
  }

  @NotNull
  @Override
  public String getVersion() {
    return myVersion;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return myId;
  }
}
