

package jetbrains.buildServer.nuget.common;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.09.11 20:49
 */
public class PackageLoadException extends Exception {
  public PackageLoadException(String message) {
    super(message);
  }

  public PackageLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
