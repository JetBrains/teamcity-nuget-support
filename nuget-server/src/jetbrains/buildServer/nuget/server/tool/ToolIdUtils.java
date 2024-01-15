

package jetbrains.buildServer.nuget.server.tool;

import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.version.VersionUtility;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Evgeniy.Koshkin
 */
public class ToolIdUtils {

  private static Pattern NuGetPackageVersionPattern = Pattern.compile(
    String.format("%s\\.(.+)", FeedConstants.NUGET_COMMANDLINE),
    Pattern.CASE_INSENSITIVE
  );

  @NotNull
  public static String getPackageVersion(@NotNull String packageName) {
    final Matcher matcher = NuGetPackageVersionPattern.matcher(packageName);
    if (matcher.matches()) {
      final String version = matcher.group(1);
      final String normalizedVersion = VersionUtility.normalizeVersion(version);
      return StringUtil.notEmpty(normalizedVersion, version);
    }
    return packageName;
  }

  @NotNull
  public static String getPackageId(@NotNull String packageName) {
    final Matcher matcher = NuGetPackageVersionPattern.matcher(packageName);
    if (matcher.matches()) {
      final String version = matcher.group(1);
      final String normalizedVersion = VersionUtility.normalizeVersion(version);
      return String.format("%s.%s",
        FeedConstants.NUGET_COMMANDLINE,
        StringUtil.notEmpty(normalizedVersion, version));
    }
    return packageName;
  }
}
