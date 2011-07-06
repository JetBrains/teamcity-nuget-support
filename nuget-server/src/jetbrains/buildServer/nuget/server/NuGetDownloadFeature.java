package jetbrains.buildServer.nuget.server;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * Created Eugene Petrenko (eugene.petrenko@gmail.com)
 * date: 28.04.11
 */
public class NuGetDownloadFeature extends BuildFeature {
  private final PluginDescriptor myDescriptor;

  public NuGetDownloadFeature(final PluginDescriptor descriptor) {
    myDescriptor = descriptor;
  }

  @NotNull
  @Override
  public String getType() {
    return "NuGet.Download";
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "NuGet Dependency Download";
  }

  @Override
  public String getEditParametersUrl() {
    return myDescriptor.getPluginResourcesPath("NuGetFetch.jsp");
  }
}
