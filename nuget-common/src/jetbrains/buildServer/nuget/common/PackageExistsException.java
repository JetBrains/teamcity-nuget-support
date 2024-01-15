

package jetbrains.buildServer.nuget.common;

/**
 * Can be used in case of conflicts.
 */
public class PackageExistsException extends Exception {
  public PackageExistsException(String message) {
    super(message);
  }

  public PackageExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
