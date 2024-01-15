

package jetbrains.buildServer.nuget.common.version;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Evgeniy.Koshkin
 */
public class FrameworkName {
  @NotNull private final String myIdentifier;
  @Nullable private final Version myVersion;
  @NotNull private final String myProfile;

  public FrameworkName(@NotNull String identifier, @Nullable Version version, @NotNull String profile) {
    myIdentifier = identifier;
    myVersion = version;
    myProfile = profile;
  }

  @NotNull
  public String getIdentifier() {
    return myIdentifier;
  }

  @Nullable
  public Version getVersion() {
    return myVersion;
  }

  @NotNull
  public String getProfile() {
    return myProfile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FrameworkName that = (FrameworkName) o;

    if (!myIdentifier.equalsIgnoreCase(that.myIdentifier)) return false;
    if (myProfile != null ? !myProfile.equalsIgnoreCase(that.myProfile) : that.myProfile != null) return false;
    if (myVersion != null ? !myVersion.equals(that.myVersion) : that.myVersion != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myIdentifier.hashCode();
    result = 31 * result + (myVersion != null ? myVersion.hashCode() : 0);
    result = 31 * result + (myProfile != null ? myProfile.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder(myIdentifier);
    if (!StringUtil.isEmpty(myProfile)) {
      builder.append(" ").append(myProfile);
    }
    if (myVersion != null) {
      builder.append(" ").append(myVersion);
    }
    return builder.toString();
  }
}
