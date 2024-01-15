

package jetbrains.buildServer.nuget.common;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 18.03.13 12:22
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public enum PackagesPackDirectoryMode {
  LEAVE_AS_IS("as_is", "Do not specify"),
  EXPLICIT_DIRECTORY("explicit", "Use explicit directory"),
  PROJECT_DIRECTORY("project", "Use project/.nuspec directory"),
  ;

  private final String myValue;
  private final String myDescription;

  private PackagesPackDirectoryMode(String value, String description) {
    myValue = value;
    myDescription = description;
  }

  @NotNull
  public String getValue() {
    return myValue;
  }

  @NotNull
  public String getDescription() {
    return myDescription;
  }

  public boolean isShowBaseDirectorySelector() {
    return this == EXPLICIT_DIRECTORY;
  }

  @Nullable
  public String getDetails() {
    switch (this) {
      case PROJECT_DIRECTORY:
        return "Specifies the -BaseDirectory parameter value to point to the directory that contains project or .nuspec files";
      case LEAVE_AS_IS:
        return "Do not add an explicit -BaseDirectory parameter";
      case EXPLICIT_DIRECTORY:
        return "The -BaseDirectory parameter value. Leave blank to use the build checkout directory";
      default:
        return null;
    }
  }

  @NotNull
  public static PackagesPackDirectoryMode fromString(@Nullable final String value) {
    if (StringUtil.isEmptyOrSpaces(value)) {
      ///this is compatibility
      return EXPLICIT_DIRECTORY;
    }
    for (PackagesPackDirectoryMode mode : values()) {
      if (mode.myValue.equals(value)) return mode;
    }
    return LEAVE_AS_IS;
  }

}
