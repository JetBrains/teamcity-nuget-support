

package jetbrains.buildServer.nuget.common.version;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Evgeniy.Koshkin
 */
public class VersionConstraint {

  @Nullable private SemanticVersion myMinVersion;
  private boolean myIsMinInclusive = true;
  @Nullable private SemanticVersion myMaxVersion = null;
  private boolean myIsMaxInclusive = true;

  @Nullable
  public static VersionConstraint valueOf(@Nullable String value) {
    if(StringUtil.isEmpty(value)) return null;
    value = value.trim();
    final VersionConstraint versionSpec = new VersionConstraint();

    final SemanticVersion version = SemanticVersion.valueOf(value);
    if ( version != null )
    {
      versionSpec.myIsMinInclusive = true;
      versionSpec.myMinVersion = version;
      return versionSpec;
    }

    if ( value.length() < 3 ) return null;

    switch ( value.charAt( 0 ) )
    {
      case '[':
        versionSpec.myIsMinInclusive = true;
        break;
      case '(':
        versionSpec.myIsMinInclusive = false;
        break;
      default:
        return null;
    }

    switch ( value.charAt( value.length() - 1 ) )
    {
      case ']':
        versionSpec.myIsMaxInclusive = true;
        break;
      case ')':
        versionSpec.myIsMaxInclusive = false;
        break;
      default:
        return null;
    }

    final String valueTrimmed = value.substring(1, value.length() - 1);
    final List<String> parts = StringUtil.split(valueTrimmed, ",");
    String minVersionString = "";
    String maxVersionString = "";
    if(parts.size() == 2){
      minVersionString = parts.get(0);
      maxVersionString = parts.get(1);
    } else if (parts.size() == 1){
      if(valueTrimmed.indexOf(',') == -1){
        minVersionString = maxVersionString = parts.get(0);
      } else{
        if(valueTrimmed.startsWith(",")){
          maxVersionString = parts.get(0);
        } else if(valueTrimmed.endsWith(",")){
          minVersionString = parts.get(0);
        } else return null;
      }
    } else return null;

    SemanticVersion minVersion = SemanticVersion.valueOf(minVersionString);
    SemanticVersion maxVersion = SemanticVersion.valueOf(maxVersionString);

    if ( minVersion == null && maxVersion == null ) return null;

    versionSpec.myMinVersion = minVersion;
    versionSpec.myMaxVersion = maxVersion;

    return versionSpec;
  }

  public boolean satisfies(@NotNull SemanticVersion version) {
    boolean condition = true;
    if (myMinVersion != null) {
      if (myIsMinInclusive)
        condition = version.compareTo(myMinVersion) >= 0;
      else
        condition = version.compareTo(myMinVersion) > 0;
    }
    if (myMaxVersion != null) {
      if (myIsMaxInclusive)
        condition = condition && version.compareTo(myMaxVersion) <= 0;
      else
        condition = condition && version.compareTo(myMaxVersion) < 0;
    }
    return condition;
  }
}
