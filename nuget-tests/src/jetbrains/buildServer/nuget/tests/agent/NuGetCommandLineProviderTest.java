

package jetbrains.buildServer.nuget.tests.agent;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.FlowLogger;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.dotNet.DotNetConstants;
import jetbrains.buildServer.nuget.agent.util.CommandLineExecutor;
import jetbrains.buildServer.nuget.agent.util.SystemInformation;
import jetbrains.buildServer.nuget.agent.util.impl.NuGetCommandLineProvider;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 08.12.11 15:34
 */
public class NuGetCommandLineProviderTest extends BaseTestCase {
  private static final String RUNNER_EXE = "runner.exe";
  private static final String MONO_PATH = "/usr/bin/mono-sgen";
  private Mockery m;
  private BuildRunnerContext myRootContext;
  private NuGetTeamCityProvider myNugetProvider;
  private File myWorkDir;
  private File myTempDir;
  private FlowLogger myLogger;
  private SystemInformation mySystemInfo;
  private Map<String, String> myConfigParameters;
  private Map<String, String> myEnvironmentVariables;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    myWorkDir = createTempDir();
    myTempDir = createTempDir();
    myRootContext = m.mock(BuildRunnerContext.class);
    myNugetProvider = m.mock(NuGetTeamCityProvider.class);
    myLogger = m.mock(FlowLogger.class);
    mySystemInfo = m.mock(SystemInformation.class);
    AgentRunningBuild build = m.mock(AgentRunningBuild.class);
    BuildParametersMap parametersMap = m.mock(BuildParametersMap.class);
    myConfigParameters = new HashMap<>();
    myEnvironmentVariables = new HashMap<>();

    m.checking(new Expectations() {{
      allowing(myRootContext).getConfigParameters(); will(returnValue(myConfigParameters));
      allowing(myRootContext).getBuildParameters(); will(returnValue(parametersMap));
      allowing(myRootContext).addEnvironmentVariable(with(any(String.class)), with(any(String.class)));
      will(new CustomAction("Add env parameter") {
        public Object invoke(Invocation invocation) {
          myEnvironmentVariables.put((String)invocation.getParameter(0), (String)invocation.getParameter(1));
          return null;
        }
      });
      allowing(myRootContext).getWorkingDirectory(); will(returnValue(myWorkDir));
      allowing(myRootContext).getBuild(); will(returnValue(build));
      allowing(build).getBuildLogger(); will(returnValue(myLogger));
      allowing(build).getBuildTempDirectory(); will(returnValue(myTempDir));
      allowing(parametersMap).getEnvironmentVariables(); will(returnValue(myEnvironmentVariables));
      allowing(myNugetProvider).getNuGetRunnerPath(); will(returnValue(new File(RUNNER_EXE)));
    }});
  }

  @Test
  public void getCommandLineForNuGetLess20() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("NuGet Version: 1.4.0");

    m.checking(new Expectations() {{
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
      oneOf(myLogger).warning("You use NuGet 1.4.0. Feed authentication is only supported from NuGet 2.0.0");
      allowing(mySystemInfo).isWindows(); will(returnValue(true));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), executable);
    Assert.assertEquals(commandLine.getArguments(), args);
  }

  @Test
  public void getCommandLineForNuGetFrom20to33() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("NuGet Version: 2.8.0");

    m.checking(new Expectations() {{
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
      allowing(mySystemInfo).isWindows(); will(returnValue(true));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), RUNNER_EXE);
    args.add(0, executable);
    Assert.assertEquals(commandLine.getArguments(), args);
  }

  @Test(expectedExceptions = RunBuildException.class)
  public void getCommandLineForNuGet28OnMono() {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("NuGet Version: 2.8.0");

    m.checking(new Expectations() {{
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
      allowing(mySystemInfo).isWindows(); will(returnValue(false));
    }});

    provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());
  }

  @Test
  public void getCommandLineForNuGetFrom33() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("nuget version: 3.4.0");

    m.checking(new Expectations() {{
      allowing(mySystemInfo).isWindows(); will(returnValue(true));
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), executable);
    Assert.assertEquals(commandLine.getArguments(), args);
  }

  @Test
  public void getCommandLineForNuGet35UnderLocalSystem() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("NuGet Version: 3.5.0");
    String packagesPath = new File(myTempDir, ".nuget/packages").getPath();

    m.checking(new Expectations() {{
      allowing(mySystemInfo).isWindows(); will(returnValue(true));
      allowing(mySystemInfo).getUserName(); will(returnValue("SYSTEM"));
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
      oneOf(myLogger).message(String.format("Setting 'NUGET_PACKAGES' environment variable to '%s'", packagesPath));
      oneOf(myLogger).message(String.format("##teamcity[setParameter name='env.NUGET_PACKAGES' value='%s']", packagesPath));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), executable);
    Assert.assertEquals(commandLine.getArguments(), args);
    Assert.assertEquals(commandLine.getEnvironment().get("NUGET_PACKAGES"), packagesPath);
  }

  @Test
  public void getCommandLineForNuGet35UnderLocalSystemWithOverridenPath() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("NuGet Version: 3.5.0");
    String packagesPath = "/path/to/packages";
    myEnvironmentVariables.put("NUGET_PACKAGES", packagesPath);

    m.checking(new Expectations() {{
      allowing(mySystemInfo).isWindows(); will(returnValue(true));
      allowing(mySystemInfo).getUserName(); will(returnValue("SYSTEM"));
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), executable);
    Assert.assertEquals(commandLine.getArguments(), args);
    Assert.assertEquals(commandLine.getEnvironment().get("NUGET_PACKAGES"), packagesPath);
  }

  @Test
  public void getCommandLineForNuGetUnderMono() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("NuGet Version: 4.3.0");
    myConfigParameters.put(DotNetConstants.MONO_JIT, MONO_PATH);

    m.checking(new Expectations() {{
      allowing(mySystemInfo).isWindows(); will(returnValue(false));
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), MONO_PATH);
    args.add(0, executable);
    Assert.assertEquals(commandLine.getArguments(), args);
  }

  @Test
  public void getCommandLineForNuGetVersionFromPath() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "tools\\NuGet.CommandLine.4.8.0-preview4.5311+3e83c3efe81f6e0611882812e863c1d2470e079a\\tools\\nuget.exe";

    m.checking(new Expectations() {{
      allowing(mySystemInfo).isWindows(); will(returnValue(true));
      allowing(mySystemInfo).getUserName(); will(returnValue("name"));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), executable);
    Assert.assertEquals(commandLine.getArguments(), args);
  }

  @Test
  public void getCommandLineForNuGet47OnWindowsWithoutCredentialsPlugin() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("NuGet Version: 4.7.0");
    String packagesPath = "/path/to/packages";
    myEnvironmentVariables.put("NUGET_PACKAGES", packagesPath);
    myEnvironmentVariables.put("NUGET_PLUGIN_PATHS", "/path");

    m.checking(new Expectations() {{
      allowing(mySystemInfo).isWindows(); will(returnValue(true));
      allowing(mySystemInfo).getUserName(); will(returnValue("user"));
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), executable);
    Assert.assertEquals(commandLine.getArguments(), args);
    Assert.assertEquals(commandLine.getEnvironment().get("NUGET_PACKAGES"), packagesPath);
    Assert.assertFalse(commandLine.getEnvironment().containsKey("NUGET_PLUGIN_PATHS"));
  }

  @Test
  public void getCommandLineForNuGet48OnWindowsWithCredentialsPlugin() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("NuGet Version: 4.8.0");
    String packagesPath = "/path/to/packages";
    myEnvironmentVariables.put("NUGET_PACKAGES", packagesPath);
    myEnvironmentVariables.put("NUGET_PLUGIN_PATHS", "/path");

    m.checking(new Expectations() {{
      allowing(mySystemInfo).isWindows(); will(returnValue(true));
      allowing(mySystemInfo).getUserName(); will(returnValue("user"));
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), executable);
    Assert.assertEquals(commandLine.getArguments(), args);
    Assert.assertEquals(commandLine.getEnvironment().get("NUGET_PACKAGES"), packagesPath);
    Assert.assertEquals(commandLine.getEnvironment().get("NUGET_PLUGIN_PATHS"), "/path");
  }

  @Test
  public void getCommandLineForNuGet48OnMonoWithoutCredentialsPlugin() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("NuGet Version: 4.8.0");
    String packagesPath = "/path/to/packages";
    myEnvironmentVariables.put("NUGET_PACKAGES", packagesPath);
    myEnvironmentVariables.put("NUGET_PLUGIN_PATHS", "/path");

    m.checking(new Expectations() {{
      allowing(mySystemInfo).isWindows(); will(returnValue(false));
      allowing(mySystemInfo).getUserName(); will(returnValue("user"));
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), executable);
    Assert.assertEquals(commandLine.getArguments(), args);
    Assert.assertEquals(commandLine.getEnvironment().get("NUGET_PACKAGES"), packagesPath);
    Assert.assertFalse(commandLine.getEnvironment().containsKey("NUGET_PLUGIN_PATHS"));
  }

  @Test
  public void getCommandLineForNuGet49OnMonoWithCredentialsPlugin() throws RunBuildException {
    CommandLineExecutor executor = m.mock(CommandLineExecutor.class);

    NuGetCommandLineProvider provider = new NuGetCommandLineProvider(myNugetProvider, executor, mySystemInfo);
    List<String> args = new ArrayList<>(Arrays.asList("arg1", "arg2"));
    String executable = "nuget.exe";
    ExecResult result = new ExecResult();
    result.setExitCode(0);
    result.setStdout("NuGet Version: 4.9.0");
    String packagesPath = "/path/to/packages";
    myEnvironmentVariables.put("NUGET_PACKAGES", packagesPath);
    myEnvironmentVariables.put("NUGET_PLUGIN_PATHS", "/path");

    m.checking(new Expectations() {{
      allowing(mySystemInfo).isWindows(); will(returnValue(false));
      allowing(mySystemInfo).getUserName(); will(returnValue("user"));
      oneOf(executor).execute(with(any(GeneralCommandLine.class))); will(returnValue(result));
    }});

    ProgramCommandLine commandLine = provider.getProgramCommandLine(myRootContext, executable, args, myWorkDir, Collections.emptyMap());

    Assert.assertNotNull(commandLine);
    Assert.assertEquals(commandLine.getWorkingDirectory(), myWorkDir.getPath());
    Assert.assertEquals(commandLine.getExecutablePath(), executable);
    Assert.assertEquals(commandLine.getArguments(), args);
    Assert.assertEquals(commandLine.getEnvironment().get("NUGET_PACKAGES"), packagesPath);
    Assert.assertEquals(commandLine.getEnvironment().get("NUGET_PLUGIN_PATHS"), "/path");
  }
}
