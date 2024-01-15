

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.runner.install.InstallStages;
import jetbrains.buildServer.nuget.agent.runner.install.PackagesReportBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerAdapter;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 21:06
 */
public class PackageInstallerBuilderReportTest extends PackageInstallerBuilderTestBase {
  @NotNull
  @Override
  protected Collection<PackagesInstallerAdapter> createBuilder(@NotNull InstallStages stages) {
    return Arrays.<PackagesInstallerAdapter>asList(new PackagesReportBuilder(myActionFactory, stages.getReportStage(), myContext));
  }


  @Test
  public void test_report() throws RunBuildException {
    m.checking(new Expectations(){{
      oneOf(myActionFactory).createUsageReport(myContext, myConfig);
      will(returnValue(createMockBuildProcess("b1")));
    }});

    doTest(t(myConfig), t("b1"));
  }

  @Test
  public void test_report_files() throws RunBuildException {
    m.checking(new Expectations(){{
      oneOf(myActionFactory).createUsageReport(myContext, myConfig);
      will(returnValue(createMockBuildProcess("b1")));

      oneOf(myActionFactory).createUsageReport(myContext, myConfig2);
      will(returnValue(createMockBuildProcess("b2")));
    }});

    doTest(t(myConfig, myConfig2), t("b1", "b2"));
  }

  @Test
  public void test_report_deleted_files() throws RunBuildException {
    m.checking(new Expectations() {{
      oneOf(myActionFactory).createUsageReport(myContext, myConfig);
      will(returnValue(createMockBuildProcess("b1")));

      oneOf(myActionFactory).createUsageReport(myContext, myConfig2);
      will(returnValue(createMockBuildProcess("b2")));
    }});


    FileUtil.delete(myConfig);
    doTest(t(myConfig, myConfig2), t("b1", "b2"));
  }

}
