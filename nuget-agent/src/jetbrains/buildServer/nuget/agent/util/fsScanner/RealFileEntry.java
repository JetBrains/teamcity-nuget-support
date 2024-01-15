

package jetbrains.buildServer.nuget.agent.util.fsScanner;

import org.jetbrains.annotations.NotNull;

public class RealFileEntry implements FileEntry {
  private final FileSystemPath myPath;

  public RealFileEntry(FileSystemPath path) {
    myPath = path;
  }

  @NotNull
  public String getName() {
    return myPath.getName();
  }

  @NotNull
  public FileSystemPath getPath() {
    return myPath;
  }

  @Override
  public String toString() {
    return "{f:" + myPath.getFilePath() + "|" + getName() + "}";
  }
}
