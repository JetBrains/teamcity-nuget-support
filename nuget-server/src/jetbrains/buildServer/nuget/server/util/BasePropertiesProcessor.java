

package jetbrains.buildServer.nuget.server.util;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 10:59
 */
public abstract class BasePropertiesProcessor implements PropertiesProcessor {
  @Nullable
  protected static String notEmpty(@NotNull final String key,
                                   @NotNull String errorMessage,
                                   @NotNull final Map<String, String> params,
                                   @NotNull final Collection<InvalidProperty> result) {
    final String value = params.get(key);
    if (StringUtil.isEmptyOrSpaces(value)) {
      result.add(new InvalidProperty(key, errorMessage));
      return null;
    }
    return value;
  }

  @NotNull
  public final Collection<InvalidProperty> process(final Map<String, String> properties) {
    Collection<InvalidProperty> result =  new ArrayList<InvalidProperty>();

    if (properties == null) return result;
    checkProperties(Collections.unmodifiableMap(properties), result);

    return result;
  }

  protected abstract void checkProperties(@NotNull final Map<String, String> map, @NotNull Collection<InvalidProperty> result);
}
