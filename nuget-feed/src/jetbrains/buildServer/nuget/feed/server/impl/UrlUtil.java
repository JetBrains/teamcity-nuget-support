

package jetbrains.buildServer.nuget.feed.server.impl;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 17.01.12 17:13
 */
public class UrlUtil {
  @NotNull
  public static String join(@NotNull String url, @NotNull String basePath) {
    return StringUtil.trimEnd(url, "/") + "/" + StringUtil.trimStart(basePath, "/");
  }

}
