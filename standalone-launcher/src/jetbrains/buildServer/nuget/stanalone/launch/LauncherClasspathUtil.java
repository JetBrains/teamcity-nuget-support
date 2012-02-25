package jetbrains.buildServer.nuget.stanalone.launch;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
/*import org.jetbrains.annotations.NotNull;*/

/**
 * User: Eugene.Petrenko
 * NOTE: This class should be compiled to be inside launcher.jar
 * because it is used with that classpath.
 */
public class LauncherClasspathUtil {
  /**
   * Converts URL to a resource (i.e. to a class) into the classpath entry on the file system.
   *
   * @param className name of the class
   * @param resUrl    this class resource Url
   * @return classpath entry
   * @throws java.io.UnsupportedEncodingException
   */
  public static String resourceUrlToClasspathEntry(/*@NotNull*/ final String className, /*@NotNull*/ final URL resUrl) throws UnsupportedEncodingException {
    final String path = classNameToResourcePath(className);
    // we have to encode '+' character manually because otherwise URLDecoder.decode will
    // transform it into the space character
    String urlStr = URLDecoder.decode(encodePlusCharacter(resUrl.toExternalForm()), "UTF-8");
    if (resUrl.getProtocol() != null) {
      // drop path within jar file only if protocol in the URL is 'jar:'
      if ("jar".equals(resUrl.getProtocol())) {
        final int jarSeparatorIndex = urlStr.indexOf("!");
        if (jarSeparatorIndex >= 0) {
          urlStr = urlStr.substring(0, jarSeparatorIndex);
        }
      }

      int startIndex = urlStr.indexOf(':');
      while (startIndex >= 0 && urlStr.charAt(startIndex + 1) != '/') {
        startIndex = urlStr.indexOf(':', startIndex + 1);
      }
      if (startIndex >= 0) {
        urlStr = urlStr.substring(startIndex + 1);
      }
    }

    if (endsWith(urlStr, path)) {
      urlStr = urlStr.substring(0, urlStr.length() - path.length());
    }

    // !Workaround for /D:/some/path/a.jar, which doesn't work if D is subst disk
    if (urlStr.startsWith("/") && urlStr.indexOf(":") == 2) {
      urlStr = urlStr.substring(1);
    }

    // URL may contain spaces, that is why we need to decode it
    return new File(urlStr).getPath();
  }

  protected static boolean endsWith(final String str, final String suffix) {
    return (suffix.length() <= str.length()) && str.regionMatches(true, str.length() - suffix.length(), suffix, 0, suffix.length());
  }

  /*@NotNull*/
  public static String getClasspathEntry(/*@NotNull*/ final Class aClass) throws IOException {
    final String path = classNameToResourcePath(aClass.getName());
    final URL resource = aClass.getResource(path);
    if (resource == null) {
      throw new IOException("Failed to find resource path for class: " + aClass);
    }
    return resourceUrlToClasspathEntry(aClass.getName(), resource);
  }

  private static String encodePlusCharacter(String orig) {
    return orig.replace("+", "%2B");
  }

  private static String classNameToResourcePath(/*@NotNull*/ final String className) {
    return "/" + className.replace('.', '/') + ".class";
  }

}
