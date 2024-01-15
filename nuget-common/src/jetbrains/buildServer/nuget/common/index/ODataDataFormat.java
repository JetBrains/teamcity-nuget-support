

package jetbrains.buildServer.nuget.common.index;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.LocalDateTime;

import java.util.Date;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 24.11.11 13:09
 */
public class ODataDataFormat {
  @NotNull
  public static String formatDate(@NotNull final Date date) {
    return "j" + date.getTime();
  }
  
  @Nullable
  public static LocalDateTime parseDate(@NotNull final String text) {
    if (!text.startsWith("j")) return null;
    return new LocalDateTime(Long.parseLong(text.substring(1)));
  }
}
