

package jetbrains.buildServer.nuget.tests.server.tools;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.tool.impl.ToolUnpacker;
import jetbrains.buildServer.util.TestFor;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 11.04.12 16:08
 */
public class ToolUnpackerTest extends BaseTestCase {

  @Test
  @TestFor(issues = "TW-21050")
  public void test_unpack_with_directories() throws IOException {
    final File zip = createTempFile();

    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));

    addZipDir(zos, "/a/b/c/d/");
    addZipDir(zos, "/a/b/c/");
    addZipDir(zos, "/a/b/");
    addZipDir(zos, "/a/");
    zos.close();

    ToolUnpacker u = new ToolUnpacker();
    File dest = createTempDir();
    u.extractPackage(zip, dest);
  }

  private void addZipDir(ZipOutputStream zos, String path) throws IOException {
    ZipEntry e = new ZipEntry(path);
    Assert.assertTrue(e.isDirectory());
    zos.putNextEntry(e);
    zos.closeEntry();
  }
}
