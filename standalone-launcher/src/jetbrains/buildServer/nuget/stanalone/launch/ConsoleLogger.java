package jetbrains.buildServer.nuget.stanalone.launch;

/**
 * Created by IntelliJ IDEA.
 * This classs is intended to handle logging in first initialization level when there is
 * no log4j initialized
 */
public class ConsoleLogger {
  public static void error(final String error) {
    System.err.println(error);
  }

  public static void error(final Throwable error) {
    error.printStackTrace(System.out);
  }

  public static void output(final String output) {
    System.out.println(output);
  }

  public static void title(final String message) {
    System.out.println(message);
  }
}
