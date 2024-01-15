

package jetbrains.buildServer.nuget.server.exec;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 18:41
 */
public class NuGetExecutionException extends Exception {
  public NuGetExecutionException(String message) {
    super(message);
  }

  public NuGetExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
