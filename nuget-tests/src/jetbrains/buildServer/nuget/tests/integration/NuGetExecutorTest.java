

package jetbrains.buildServer.nuget.tests.integration;


import jetbrains.buildServer.TempFolderProvider;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutor;
import jetbrains.buildServer.nuget.server.exec.NuGetOutputProcessor;
import jetbrains.buildServer.nuget.server.exec.impl.NuGetExecutorImpl;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 18:40
 */
public class NuGetExecutorTest extends IntegrationTestBase {
  private Mockery m;
  private NuGetTeamCityProvider info;
  private NuGetExecutor exec;
  private SystemInfo mySystemInfo;
  private TempFolderProvider myTempDir;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    info = m.mock(NuGetTeamCityProvider.class);
    mySystemInfo = m.mock(SystemInfo.class);
    myTempDir = m.mock(TempFolderProvider.class);
    exec = new NuGetExecutorImpl(info, mySystemInfo, myTempDir);

    m.checking(new Expectations(){{
      allowing(info).getNuGetRunnerPath(); will(returnValue(Paths.getNuGetRunnerPath()));
      allowing(info).getCredentialProviderHomeDirectory(); will(returnValue(Paths.getCredentialProviderHomeDirectory()));
      allowing(myTempDir).getTempDirectory(); will(returnValue(Paths.getTestDataPath()));
    }});
  }

  private void setIsWindows(final boolean isWindows) {
    m.checking(new Expectations(){{
      allowing(mySystemInfo).canStartNuGetProcesses(); will(returnValue(isWindows));
    }});
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_ping_windows(@NotNull final NuGet nuget) throws NuGetExecutionException {
    if(!com.intellij.openapi.util.SystemInfo.isWindows) {
      return;
    }

    setIsWindows(true);
    doPingTest(nuget);
  }

  @Test
  public void test_does_not_run_on_linux() throws NuGetExecutionException {
    setIsWindows(false);
    try {
      doPingTest(NuGet.NuGet_1_8);
    } catch (NuGetExecutionException e) {
      return;
    }
    Assert.fail("Exception expected");
  }

  private void doPingTest(NuGet nuget) throws NuGetExecutionException {
    int code = exec.executeNuGet(
            nuget.getPath(),
            Collections.singletonList("TeamCity.Ping"), Collections.<PackageSource>emptyList(),
            new NuGetOutputProcessor<Integer>() {
              private int myExitCode;
      public void onStdOutput(@NotNull String text) {
        System.out.println(text);
      }

      public void onStdError(@NotNull String text) {
        System.out.println(text);
      }

      public void onFinished(int exitCode) {
        System.out.println("Exit Code: " + exitCode);
        myExitCode = exitCode;
      }

      @NotNull
      public Integer getResult() {
        return myExitCode;
      }
    });

    Assert.assertEquals(code, 0);
  }
}
