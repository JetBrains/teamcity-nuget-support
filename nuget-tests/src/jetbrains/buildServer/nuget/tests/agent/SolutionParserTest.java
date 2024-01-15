

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.nuget.agent.util.sln.SolutionFileParser;
import jetbrains.buildServer.nuget.agent.util.sln.impl.SolutionParserImpl;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.util.FileUtil;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 15.06.12 20:06
 */
public class SolutionParserTest extends BaseTestCase {
  private Mockery m;
  private BuildProgressLogger myLogger;
  private SolutionFileParser myParser = new SolutionParserImpl();

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    myLogger = m.mock(BuildProgressLogger.class);
  }

  @Test
  public void test_vs2008() throws IOException, RunBuildException {
    doTest("vs2008.sln",
            "SystemCoreReferenced" + File.separator + "SystemCoreReferenced.csproj",
            "SystemCoreNotReferenced" + File.separator + "SystemCoreNotReferenced.csproj");
  }

  @Test
  public void test_vs2010() throws IOException, RunBuildException {
    doTest( "vs2010.sln",
            "SystemCoreReferenced/SystemCoreReferenced.csproj",
            "SystemCoreNotReferenced_ImplicitAllowed/SystemCoreNotReferenced_ImplicitAllowed.csproj",
            "SystemCoreNotReferenced_ImplicitNotAllo/SystemCoreNotReferenced_ImplicitNotAllo.csproj",
            "NotDefaultImplicitReference/NotDefaultImplicitReference.csproj");
  }

  @Test
  public void test_vs2010_maxi() throws IOException, RunBuildException {
    doTest( "Lunochod1.sln",
            "Lunochod1" + File.separator + "Lunochod1.csproj",
            "Lunochod2" + File.separator + "Lunochod2.vcxproj",
            "Lunochod3" + File.separator + "Lunochod3.vbproj",
            "Lunochod5" + File.separator + "Lunochod5.csproj",
            "Lunochod6" + File.separator + "Lunochod6.csproj",
            "Lunochod6.Tests" + File.separator + "Lunochod6.Tests.csproj"
            );
  }

  @Test
  public void test_webSite() throws IOException, RunBuildException {
    doTest( "WebSiteReferencedProjects.sln",
            ".." + File.separator + ".." + File.separator + "WebSites" + File.separator + "WebSite2",
            "ClassLibrary1" + File.separator + "ClassLibrary1.csproj"
            );
  }

  @Test
  public void test_webSite11_1() throws IOException, RunBuildException {
    m.checking(new Expectations(){{
      allowing(myLogger).warning(with(any(String.class)));
    }});

    doTest("VS11Website.sln",
            "e:" + File.separator + "temp" + File.separator + "VS11Website"
    );
  }

  @Test
  public void test_webSite11_2() throws IOException, RunBuildException {
    m.checking(new Expectations() {{
      allowing(myLogger).warning(with(any(String.class)));
    }});

    doTest( "VS11Website2.sln",
            ".." + File.separator + ".." + File.separator + "WebSites" + File.separator + "WebSite1" + File.separator + "",
            "Test",
            "WebApplication1" + File.separator + "WebApplication1.csproj"
            );
  }

  @Test
  public void test_webSite11_ts() throws IOException, RunBuildException {
    doTest( "IncorrectTreeSectionStructure.sln",
            "CSSL4MusicPlayer" + File.separator + "CSSL4MusicPlayer.Web" + File.separator + "CSSL4MusicPlayer.Web.csproj",
            "CSSL4MusicPlayer" + File.separator + "CSSL4MusicPlayer" + File.separator + "CSSL4MusicPlayer.csproj"
            );
  }

  @Test
  public void test_webSite11_base() throws IOException, RunBuildException {
    doTest( "webProject2010.sln",
            "e:" + File.separator + "Temp" + File.separator + "x44"
            );
  }

  @Test
  public void test_projectData() throws IOException, RunBuildException {
    doTest( "ProjectsData.sln",
            "WebSite",
            "MVC_WebApp" + File.separator + "MVC_WebApp.csproj",
            "MVC2_WebApp" + File.separator + "MVC2_WebApp.csproj"
            );
  }

  @NotNull
  private File getTestDataPath(@NotNull String path) {
    return Paths.getTestDataPath("sln/" + path);
  }

  private void doTest(String slnName, String... relPaths) throws IOException, RunBuildException {
    final File sln = getTestDataPath(slnName);
    final Collection<File> projects = new TreeSet<File>();
    for (String path : relPaths) {
      projects.add(FileUtil.resolvePath(sln.getParentFile(), path));
    }

    Assert.assertEquals(projects, new TreeSet<File>(myParser.parseProjectFiles(myLogger, sln)));
  }

}
