package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.nuget.agent.commands.impl.CommandFactoryImpl;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetActionFactoryImpl;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created 13.08.13 12:05
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetRestorePackageActionFactoryTest extends BaseTestCase {
  private Mockery m;
  private CommandlineBuildProcessFactory myProcessFactory;
  private NuGetActionFactoryImpl i;
  private BuildRunnerContext ctx;
  private PackagesInstallParameters ps;
  private NuGetFetchParameters nugetParams;
  private File myTarget;
  private File myConfig;
  private BuildParametersMap myBuildParametersMap;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myProcessFactory = m.mock(CommandlineBuildProcessFactory.class);
    PackageUsages pu = m.mock(PackageUsages.class);
    i = new NuGetActionFactoryImpl(myProcessFactory, pu, new CommandFactoryImpl());
    ctx = m.mock(BuildRunnerContext.class);
    ps = m.mock(PackagesInstallParameters.class);
    nugetParams = m.mock(NuGetFetchParameters.class);

    myTarget = createTempDir();
    myConfig = createTempFile();

    myBuildParametersMap = m.mock(BuildParametersMap.class);

    m.checking(new Expectations(){{
      allowing(ctx).getBuildParameters(); will(returnValue(myBuildParametersMap));
      allowing(ps).getNuGetParameters(); will(returnValue(nugetParams));
    }});
  }

  @Test
  public void test_no_sources() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(nugetParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getExcludeVersion(); will(returnValue(false));
      allowing(ps).getNoCache(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("restore", myConfig.getPath(), "-OutputDirectory", myTarget.getPath()),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createRestore(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_no_sources_no_cache() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(nugetParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getExcludeVersion(); will(returnValue(false));
      allowing(ps).getNoCache(); will(returnValue(true));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("restore", myConfig.getPath(), "-NoCache", "-OutputDirectory", myTarget.getPath()),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createRestore(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_no_sources_excludeVersion() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Collections.<String>emptyList()));
      allowing(nugetParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getExcludeVersion(); will(returnValue(true));
      allowing(ps).getNoCache(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("restore", myConfig.getPath(), "-ExcludeVersion", "-OutputDirectory", myTarget.getPath()),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createRestore(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }

  @Test
  public void test_sources() throws RunBuildException, IOException {
    final File nuget = createTempFile();
    m.checking(new Expectations(){{
      allowing(nugetParams).getNuGetPackageSources(); will(returnValue(Arrays.asList("aaa", "bbb")));
      allowing(nugetParams).getNuGetExeFile();  will(returnValue(nuget));
      allowing(ps).getExcludeVersion(); will(returnValue(false));
      allowing(ps).getNoCache(); will(returnValue(false));

      oneOf(myProcessFactory).executeCommandLine(
              ctx,
              nuget.getPath(),
              Arrays.asList("restore", myConfig.getPath(), "-OutputDirectory", myTarget.getPath(), "-Source", "aaa", "-Source", "bbb"),
              myConfig.getParentFile(),
              Collections.<String, String>emptyMap()
      );
    }});

    i.createRestore(ctx, ps, myConfig, myTarget);
    m.assertIsSatisfied();
  }
}

