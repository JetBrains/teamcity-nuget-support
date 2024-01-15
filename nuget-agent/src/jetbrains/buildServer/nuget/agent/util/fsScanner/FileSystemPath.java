

package jetbrains.buildServer.nuget.agent.util.fsScanner;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FileSystemPath {
  private final File myPath;

  public FileSystemPath(@NotNull String path) {
    while (path.endsWith("/") || path.endsWith("\\"))
      path = path.substring(0, path.length() - 1);
    myPath = new File(path);
  }

  public FileSystemPath(@NotNull final File path) {
    myPath = path;
  }

  @NotNull
  public String getName() {

    String name = myPath.getName();
    if (name == null || name.length() == 0) return myPath.getPath();
    return name;

  }

  @NotNull
  public File getFilePath() {
    return myPath;
  }
}
