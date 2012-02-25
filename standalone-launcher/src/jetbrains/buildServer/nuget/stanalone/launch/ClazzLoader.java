package jetbrains.buildServer.nuget.stanalone.launch;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Eugene Petrenko
 *         Created: 09.06.2008 20:58:42
 */
public class ClazzLoader {
  public static final String PROTO = "jresource42";

  public static void callMain2(final String clazz, final String mainMethod, final String[] args) {
    call(clazz, mainMethod, args);
  }

  private static void call(final String clazz, final String mainMethod, final String[] args) {
    final File jar = thisJar(ClazzLoader.class);
    if (jar == null) {
      return;
    }

    final List<URL> jars = listEmbeddedJars(jar);
    final URL[] urls = jars.toArray(new URL[jars.size()]);

    final URLClassLoader cl = new URLClassLoader(urls, null);
    Thread.currentThread().setContextClassLoader(cl);
    try {

      final Class<?> aClass = cl.loadClass(clazz);
      final Method method = aClass.getMethod(mainMethod, String[].class);

      if (aClass.getClassLoader() != cl) {
        ConsoleLogger.error("Failed to load class " + clazz + " using new classloader");
      }  else {      
        method.invoke(null, new Object[] {args});
      }
    } catch (Throwable e) {
      ConsoleLogger.error(e);
    }
  }

  private static List<URL> listEmbeddedJars(File jar) {
    final List<URL> jars = new ArrayList<URL>();
    ZipInputStream zis = null;
    try {
      zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(jar)));
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        final String name = ze.getName();
        if (name.startsWith("lib") && name.endsWith(".jar")) {
          System.out.println("Found: " + name);
          final URL res = patchURL("/" + name);
          if (res != null) {
            jars.add(res);
          }
        }
      }
    } catch (IOException e) {
      ConsoleLogger.error(e);
    } finally {
      if (zis != null) {
        try {
          zis.close();
        } catch (IOException e) {
          //NOP
        }
      }
    }
    System.out.println("jars = " + jars);
    return jars;
  }

  private static URL patchURL(String name) throws MalformedURLException {
    return new URL(PROTO, PROTO, 42, name, new MyURLStreamHandler());
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

  private static class MyURLStreamHandler extends URLStreamHandler {
    private final ClassLoader myClazzLoader = getClass().getClassLoader();
    @Override
    protected URLConnection openConnection(final URL u) throws IOException {
      final String file = u.getFile();
      System.out.println("open connection: " + file);
      System.out.flush();
      final URL res = myClazzLoader.getResource(file);
      if (res == null) {
        System.out.println("Resource not found: " + u);
        System.out.flush();
        throw new FileNotFoundException("Failed to find: " + u);
      }
      try {
        return res.openConnection();
      } catch (Throwable t) {
        System.out.println("Resource connection openned: " + file + " -> " + u);
        System.out.flush();
        throw new IOException(t);
      }
    }
  }
}
