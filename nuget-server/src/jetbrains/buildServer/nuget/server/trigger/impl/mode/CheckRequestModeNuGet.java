

package jetbrains.buildServer.nuget.server.trigger.impl.mode;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.09.11 16:45
 */
public class CheckRequestModeNuGet implements CheckRequestMode {
  private final File myNuGetPath;

  public CheckRequestModeNuGet(@NotNull File nuGetPath) {
    myNuGetPath = nuGetPath;
  }

  @NotNull
  public File getNuGetPath() {
    return myNuGetPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CheckRequestModeNuGet that = (CheckRequestModeNuGet) o;
    return myNuGetPath.equals(that.myNuGetPath);
  }

  @Override
  public int hashCode() {
    return myNuGetPath.hashCode();
  }
}
