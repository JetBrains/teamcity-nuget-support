

package jetbrains.buildServer.nuget.agent.util.fsScanner;

import org.jetbrains.annotations.NotNull;

public interface FileSystem {
  boolean isPathAbsolute(@NotNull String path);

  @NotNull
  DirectoryEntry getRoot();

  boolean caseSensitive();
}
