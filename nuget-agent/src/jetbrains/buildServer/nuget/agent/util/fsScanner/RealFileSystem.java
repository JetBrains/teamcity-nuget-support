

package jetbrains.buildServer.nuget.agent.util.fsScanner;

import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class RealFileSystem implements FileSystem {
  public static final DirectoryEntry ROOT = new RealRootDirectory();

  public boolean isPathAbsolute(@NotNull String path) {
    if (SystemInfo.isWindows) {
      if ((path.startsWith("/") || path.startsWith("\\"))) return false;
      return new File(path).isAbsolute();
    }
    return path.startsWith("/");
  }

  @NotNull
  public DirectoryEntry getRoot() {
    return ROOT;
  }

  public boolean caseSensitive() {
    return SystemInfo.isFileSystemCaseSensitive;
  }
}
