

package jetbrains.buildServer.nuget.agent.util.fsScanner;

import org.jetbrains.annotations.NotNull;

public interface FileEntry {
  @NotNull
  String getName();

  @NotNull
  FileSystemPath getPath();
}
