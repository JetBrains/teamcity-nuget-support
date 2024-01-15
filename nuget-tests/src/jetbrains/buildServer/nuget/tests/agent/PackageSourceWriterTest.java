

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import jetbrains.buildServer.nuget.common.auth.PackageSourceUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created 04.01.13 19:43
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class PackageSourceWriterTest extends BaseTestCase {
  private PackageSourceUtil myWriter = new PackageSourceUtil();

  @Test
  public void testSerialize_one() throws IOException {
    final File tmp = createTempFile();

    myWriter.writeSources(tmp, t(source("a", "b", "c")));

    final String xml = new String(FileUtil.loadFileText(tmp, "utf-8"));
    final String reformatted = StringUtil.convertLineSeparators(XmlUtil.to_s(XmlUtil.from_s(xml)));
    final String gold = StringUtil.convertLineSeparators("<sources>\n" +
            "  <source source=\"a\" username=\"b\" password=\"c\" />\n" +
            "</sources>");

    System.out.println(reformatted);
    Assert.assertEquals(reformatted, gold);
  }

  @Test
  public void testSerialize_some() throws IOException {
    final File tmp = createTempFile();

    myWriter.writeSources(tmp, t(source("a", "b", "c"), source("qqq", null, null)));

    final String xml = new String(FileUtil.loadFileText(tmp, "utf-8"));
    final String reformatted = StringUtil.convertLineSeparators(XmlUtil.to_s(XmlUtil.from_s(xml)));
    final String gold = StringUtil.convertLineSeparators("<sources>\n" +
            "  <source source=\"a\" username=\"b\" password=\"c\" />\n" +
            "  <source source=\"qqq\" />\n" +
            "</sources>");

    System.out.println(reformatted);
    Assert.assertEquals(reformatted, gold);
  }


  @NotNull
  private static <T> Collection<T> t(T... ts) {
    return Arrays.asList(ts);
  }

  @NotNull
  private PackageSource source(@NotNull final String feed,
                               @Nullable final String user,
                               @Nullable final String pass) {
    return new PackageSource() {
      @NotNull
      public String getSource() {
        return feed;
      }

      @Nullable
      public String getUsername() {
        return user;
      }

      @Nullable
      public String getPassword() {
        return pass;
      }
    };
  }
}
