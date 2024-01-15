

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesCollectorImpl;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesConfigParser;
import jetbrains.buildServer.nuget.common.NuGetPackageInfo;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.07.11 2:01
 */
public class NuGetPackagesConfigParserTest extends BaseTestCase {

  @Test
  public void test_01() throws IOException {
    doTest("test-01.xml", new NuGetPackageInfo("elmah", "1.1"));
  }

  @Test
  public void test_02() throws IOException {
    doTest("test-02.xml", new NuGetPackageInfo("elmah", "1.1"));
  }

  @Test
  public void test_03() throws IOException {
    doTest("test-03.xml",
            new NuGetPackageInfo("Machine.Specifications", "0.4.13.0"),
            new NuGetPackageInfo("NUnit", "2.5.7.10213")
            )    ;
  }

  @Test
  public void test_04() throws IOException {
    doTest("test-04.xml",
            new NuGetPackageInfo("EasyHttp", "1.0.6"),
            new NuGetPackageInfo("JsonFx", "2.0.1106.2610"),
            new NuGetPackageInfo("structuremap", "2.6.2"));
  }

  public void doTest(@NotNull String testData,
                     @NotNull NuGetPackageInfo... packages) throws IOException {
    NuGetPackagesConfigParser p = new NuGetPackagesConfigParser();
    Mockery m = new Mockery();
    BuildAgentConfiguration configuration = m.mock(BuildAgentConfiguration.class);
    m.checking(new Expectations() {{
      oneOf(configuration).getServerUrl();
      will(returnValue("http://localhost:8080"));
    }});

    NuGetPackagesCollectorImpl i = new NuGetPackagesCollectorImpl(configuration);
    p.parseNuGetPackages(Paths.getTestDataPath("config/" + testData), i);

    if (packages.length != i.getUsedPackages().getUsedPackages().size()) {
      System.out.println(i.getUsedPackages());
    }

    Assert.assertEquals(
            new TreeSet<NuGetPackageInfo>(i.getUsedPackages().getUsedPackages()),
            new TreeSet<NuGetPackageInfo>(Arrays.asList(packages)));
  }
}
