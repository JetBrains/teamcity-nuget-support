

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.runner.install.InstallStages;
import jetbrains.buildServer.nuget.agent.runner.install.PackagesInstallerBuilder;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.PackagesInstallerAdapter;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.06.12 21:02
 */
public class PackageInstallerBuilderInstallTest extends PackageInstallerBuilderTestBase {
  @NotNull
  @Override
  protected Collection<PackagesInstallerAdapter> createBuilder(@NotNull InstallStages stages) {
    return Arrays.<PackagesInstallerAdapter>asList(new PackagesInstallerBuilder(myActionFactory, stages.getInstallStage(), myContext, myInstall));
  }

  @Test
  public void test_install_no_update() throws RunBuildException {
    m.checking(new Expectations() {{
      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("b1")));
    }});

    doTest(t(myConfig), t("b1"));
  }

  @Test
  public void test_restore_no_update() throws RunBuildException {
    myInstallMode = PackagesInstallMode.VIA_RESTORE;
    m.checking(new Expectations() {{
      oneOf(myActionFactory).createRestoreForSolution(myContext, myInstall, mySln);
      will(returnValue(createMockBuildProcess("b1")));
    }});

    doTest(t(myConfig), t("b1"));
  }

  @Test
  public void test_install_no_update_may() throws RunBuildException {
    m.checking(new Expectations(){{
      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig, myTaget);
      will(returnValue(createMockBuildProcess("b1")));
      oneOf(myActionFactory).createInstall(myContext, myInstall, myConfig2, myTaget);
      will(returnValue(createMockBuildProcess("b2")));

    }});

    doTest(t(myConfig, myConfig2), t("b1", "b2"));
  }



}
