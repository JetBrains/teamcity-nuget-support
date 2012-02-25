package jetbrains.buildServer.nuget.stanalone.launch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Eugene Petrenko
 *         Created: 09.06.2008 20:58:42
 */
public class ClazzLoader {
  public static void callMain2(final String clazz, final String mainMethod, final String[] args) {
    call(clazz, mainMethod, args);
  }

  private static void call(final String clazz, final String mainMethod, final String[] args) {
    final File jar = thisJar(ClazzLoader.class);
    if (jar == null) {
      return;
    }

    final List<URL> jars = listEmbeddedJars(jar);
    if (jars.isEmpty()) {
      System.err.println("Failed to find modules to load. Please check all files are unpacked right. Exiting");
      System.exit(1);
    }
    final URL[] urls = jars.toArray(new URL[jars.size()]);

    final URLClassLoader cl = new URLClassLoader(urls, null);
    Thread.currentThread().setContextClassLoader(cl);
    try {

      final Class<?> aClass = cl.loadClass(clazz);
      final Method method = aClass.getMethod(mainMethod, String[].class);

      if (aClass.getClassLoader() != cl) {
        ConsoleLogger.error("Failed to load class " + clazz + " using new classloader");
      } else {
        method.invoke(null, new Object[]{args});
      }
    } catch (Throwable e) {
      ConsoleLogger.error(e);
    }
  }

  private static List<URL> listEmbeddedJars(File jar) {
    final File lisz = new File(jar.getParentFile(), "lib");
    if (!lisz.isDirectory()) {
      System.err.println("Failed to find lib directory.");
      return Collections.emptyList();
    }

    final List<URL> jars = new ArrayList<URL>();

    final File[] all = lisz.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".jar");
      }
    });

    if (all == null) {
      System.err.println("Failed to find files under lib directory.");
      return Collections.emptyList();
    }

    try {
      for (File file : all) {
        jars.add(file.toURI().toURL());
      }
    } catch (MalformedURLException e) {
      System.err.println("Failed to create URL from a File");
      return Collections.emptyList();
    }
    return jars;
  }

  private static File thisJar(final Class clazz) {
    try {
      final File home = new File(LauncherClasspathUtil.getClasspathEntry(clazz));
      if (home.isFile()) {
        return home;
      }
      ConsoleLogger.error("Failed to find our .jar");
      return null;
    } catch (IOException e) {
      ConsoleLogger.error(e);
      return null;
    }
  }
}
