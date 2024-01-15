

package jetbrains.buildServer.nuget.server.tool.impl;

import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 15:11
 */
public class ToolUnpacker {
  public void extractPackage(@NotNull final File pkg,
                             @NotNull final File dest) throws IOException {
    FileUtil.createDir(dest);
    if (!ArchiveUtil.unpackZip(pkg, "", dest)) {
      throw new IOException("Failed to unpack package " + pkg + " to " + dest);
    }
  }
}
