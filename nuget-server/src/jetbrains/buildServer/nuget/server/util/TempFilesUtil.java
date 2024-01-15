

package jetbrains.buildServer.nuget.server.util;

import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Created by Evgeniy.Koshkin on 17.12.2015
 */
public class TempFilesUtil {
  public static File createTempFile(@NotNull File location, @NotNull String infix) throws NuGetExecutionException {
    try {
      location.mkdirs();
      return FileUtil.createTempFile(location, "nuget", infix + ".xml", true);
    } catch (IOException e) {
      throw new NuGetExecutionException("Failed to create temp file at " + location + ". " + e.getMessage(), e);
    }
  }
}
