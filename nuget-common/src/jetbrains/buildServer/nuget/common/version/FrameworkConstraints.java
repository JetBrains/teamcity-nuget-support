

package jetbrains.buildServer.nuget.common.version;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Evgeniy.Koshkin
 */
public class FrameworkConstraints {

  private static final String SEPARATOR = "|";

  @NotNull
  public static String convertToString(Collection<String> constraints){
    return StringUtil.join("|", constraints);
  }

  @NotNull
  public static Set<String> convertFromString(@Nullable String string){
    if(string == null) return Collections.emptySet();
    return new HashSet<String>(StringUtil.split(string, SEPARATOR));
  }
}
