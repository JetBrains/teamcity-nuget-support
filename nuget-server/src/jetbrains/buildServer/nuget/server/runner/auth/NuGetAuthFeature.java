

package jetbrains.buildServer.nuget.server.runner.auth;

import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 04.01.13 11:09
 */
public class NuGetAuthFeature extends BuildFeature {
  private final PluginDescriptor myDescriptor;
  private final AuthBean myKeys = new AuthBean();

  public NuGetAuthFeature(@NotNull PluginDescriptor descriptor) {
    myDescriptor = descriptor;
  }

  @NotNull
  @Override
  public String getType() {
    return PackagesConstants.AUTH_FEATURE_TYPE;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "NuGet feed credentials";
  }

  @NotNull
  @Override
  public String describeParameters(@NotNull Map<String, String> params) {
    StringBuilder sb = new StringBuilder();
    sb.append("Feed:").append(params.get(myKeys.getFeedKey()));
    return sb.toString();
  }

  @Nullable
  @Override
  public PropertiesProcessor getParametersProcessor() {
    return new PropertiesProcessor() {
      public Collection<InvalidProperty> process(Map<String, String> properties) {
        final List<InvalidProperty> problems = new ArrayList<InvalidProperty>();
        if (properties == null) return problems;

        if (StringUtil.isEmptyOrSpaces(properties.get(myKeys.getFeedKey()))) {
          problems.add(new InvalidProperty(myKeys.getFeedKey(), "Feed URI must be specified"));
        }

        if (StringUtil.isEmptyOrSpaces(properties.get(myKeys.getUsernameKey()))) {
          problems.add(new InvalidProperty(myKeys.getUsernameKey(), "Username must be specified"));
        }

        if (StringUtil.isEmptyOrSpaces(properties.get(myKeys.getPasswordKey()))) {
          problems.add(new InvalidProperty(myKeys.getPasswordKey(), "Password must be specified"));
        }

        return problems;
      }
    };
  }

  @Override
  public boolean isMultipleFeaturesPerBuildTypeAllowed() {
    return true;
  }

  @Nullable
  @Override
  public String getEditParametersUrl() {
    return myDescriptor.getPluginResourcesPath("auth/authFeature.jsp");
  }
}
