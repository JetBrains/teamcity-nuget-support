

package jetbrains.buildServer.nuget.agent.util.fsScanner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface DirectoryEntry {
  @NotNull
  String getName();

  @Nullable
  DirectoryEntry getParent();

  @NotNull
  DirectoryEntry[] getSubdirectories();

  /**
   * @param names names filter
   * @return redured list of items filtered (if possible) by given names
   */
  @NotNull
  DirectoryEntry[] getSubdirectories(Collection<String> names);

  @NotNull
  FileEntry[] getFiles();

  /**
   * @param names names filter
   * @return returns list of existing file names, i.e. subset of given names
   */
  @NotNull
  FileEntry[] getFiles(@NotNull Collection<String> names);
}
