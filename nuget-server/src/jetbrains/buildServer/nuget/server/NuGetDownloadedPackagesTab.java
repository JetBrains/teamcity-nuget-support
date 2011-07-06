package jetbrains.buildServer.nuget.server;

import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created Eugene Petrenko (eugene.petrenko@gmail.com)
 * date: 28.04.11
 */
public class NuGetDownloadedPackagesTab extends ViewLogTab {
  public NuGetDownloadedPackagesTab(@NotNull final PagePlaces pagePlaces,
                                    @NotNull final SBuildServer server,
                                    @NotNull final PluginDescriptor descriptor) {
    super("NuGet Packages", "xxx", pagePlaces, server);
    setIncludeUrl(descriptor.getPluginResourcesPath("NuGetBuild.jsp"));
    register();
  }

  @Override
  public boolean isAvailable(@NotNull final HttpServletRequest request) {
    final SBuild build = getBuild(request);
    return super.isAvailable(request) && build != null && build.getParametersProvider().get("nuget.packages") != null;
  }

  @Override
  protected void fillModel(final Map model, final HttpServletRequest request, @Nullable final SBuild build) {
    final Map<String, String> packages = new TreeMap<String, String>();
    packages.put("ELMAH", "1.2.0");
    packages.put("Antlr", "4.0.0");
    packages.put("EasyHTTP", "1.1");
    packages.put("JsonFX", "2.0");
    packages.put("structuremap", "2.6.2");
    packages.put("Awsome", "1.5.0");

    //noinspection unchecked
    model.put("nugetPackages", packages);
  }
}
