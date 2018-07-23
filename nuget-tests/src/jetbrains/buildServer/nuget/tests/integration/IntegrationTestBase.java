/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.tests.integration;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.StreamUtil;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.commands.impl.*;
import jetbrains.buildServer.nuget.agent.dependencies.NuGetPackagesCollector;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.dependencies.impl.*;
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.nuget.agent.util.BuildProcessBase;
import jetbrains.buildServer.nuget.agent.util.CommandlineBuildProcessFactory;
import jetbrains.buildServer.nuget.agent.util.impl.CommandLineExecutorImpl;
import jetbrains.buildServer.nuget.agent.util.impl.NuGetCommandBuildProcessFactory;
import jetbrains.buildServer.nuget.agent.util.impl.NuGetCommandLineProvider;
import jetbrains.buildServer.nuget.common.PackageInfoLoader;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProviderBase;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 22.07.11 1:26
 */
public class IntegrationTestBase extends BuildProcessTestCase {
  private StringBuilder myCommandsOutput;
  protected File myRoot;
  protected Mockery m;
  protected AgentRunningBuild myBuild;
  protected BuildRunnerContext myContext;
  protected BuildProgressLogger myLogger;
  protected PackagesParametersFactory myParametersFactory;
  protected NuGetFetchParameters myFetchParameters;
  protected NuGetPackagesCollector myCollector;
  protected NuGetActionFactory myActionFactory;
  private BuildProcess myMockProcess;
  protected BuildParametersMap myBuildParametersMap;
  protected CommandlineBuildProcessFactory myExecutor;
  protected NuGetTeamCityProvider myNuGetTeamCityProvider;
  protected String cmd;
  protected Set<PackageSource> myGlobalSources;
  private ExtensionHolder myExtensionHolder;
  protected PackageSourceManager myPsm;

  @NotNull
  protected String getCommandsOutput() {
    return myCommandsOutput.toString();
  }


  public static final String NUGET_VERSIONS = "nuget_versions";
  public static final String NUGET_VERSIONS_15p = "nuget_versions_15p";
  public static final String NUGET_VERSIONS_16p = "nuget_versions_16p";
  public static final String NUGET_VERSIONS_17p = "nuget_versions_17p";
  public static final String NUGET_VERSIONS_18p = "nuget_versions_18p";
  public static final String NUGET_VERSIONS_20p = "nuget_versions_20p";
  public static final String NUGET_VERSIONS_27p = "nuget_versions_27p";
  public static final String NUGET_VERSIONS_28p = "nuget_versions_28p";

  @NotNull
  protected Object[][] versionsFrom(@NotNull final NuGet lowerBound) {
    final NuGet[] values =  NuGet.values();
    final List<Object[]> data = new ArrayList<Object[]>();
    for (NuGet value : values) {
      if(!SystemInfo.isWindows) {
        if(!((value.major == 3 && value.minor != 2) || (value.major == 2 && value.minor == 8))) {
          continue;
        }
      }

      if (value.major < lowerBound.major) continue;
      if (value.major == lowerBound.major && value.minor < lowerBound.minor) continue;
      data.add(new Object[]{value});
    }
    return data.toArray(new Object[data.size()][]);
  }

  @DataProvider(name = NUGET_VERSIONS)
  public Object[][] dataProviderNuGetVersions() {
    return versionsFrom(NuGet.NuGet_1_6);
  }

  @DataProvider(name = NUGET_VERSIONS_15p)
  public Object[][] dataProviderNuGetVersions15p() {
    return versionsFrom(NuGet.NuGet_1_6);
  }

  @DataProvider(name = NUGET_VERSIONS_16p)
  public Object[][] dataProviderNuGetVersions16p() {
    return versionsFrom(NuGet.NuGet_1_6);
  }

  @DataProvider(name = NUGET_VERSIONS_17p)
  public Object[][] dataProviderNuGetVersions17p() {
    return versionsFrom(NuGet.NuGet_1_7);
  }

  @DataProvider(name = NUGET_VERSIONS_18p)
  public Object[][] dataProviderNuGetVersions18p() {
    return versionsFrom(NuGet.NuGet_1_8);
  }

  @DataProvider(name = NUGET_VERSIONS_20p)
  public Object[][] dataProviderNuGetVersions20p() {
    return versionsFrom(NuGet.NuGet_2_0);
  }

  @DataProvider(name = NUGET_VERSIONS_27p)
  public Object[][] dataProviderNuGetVersions27p() {
    return versionsFrom(NuGet.NuGet_2_7);
  }

  @DataProvider(name = NUGET_VERSIONS_28p)
  public Object[][] dataProviderNuGetVersions28p() {
    return versionsFrom(NuGet.NuGet_2_8);
  }

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myGlobalSources = new HashSet<>();
    myCommandsOutput = new StringBuilder();
    myRoot = createTempDir();
    m = new Mockery();
    myBuild = m.mock(AgentRunningBuild.class);
    myContext = m.mock(BuildRunnerContext.class);
    myLogger = m.mock(FlowLogger.class);
    myParametersFactory = m.mock(PackagesParametersFactory.class);
    myMockProcess = m.mock(BuildProcess.class);
    myFetchParameters = m.mock(NuGetFetchParameters.class);
    myBuildParametersMap = m.mock(BuildParametersMap.class);
    File extensionsPath = Paths.getCredentialProviderHomeDirectory().getParentFile().getParentFile();
    myNuGetTeamCityProvider = new NuGetTeamCityProviderBase(extensionsPath);
    myExtensionHolder = m.mock(ExtensionHolder.class);

    cmd = System.getenv("ComSpec");
    myPsm = m.mock(PackageSourceManager.class);

    final Map<String, String> configParameters = new TreeMap<>();
    final Map<String, String> envParameters = new TreeMap<>();
    envParameters.put("ComSpec", cmd);

    m.checking(new Expectations(){{
      allowing(myContext).getBuildParameters(); will(returnValue(myBuildParametersMap));
      allowing(myContext).getConfigParameters(); will(returnValue(Collections.emptyMap()));
      allowing(myContext).getWorkingDirectory(); will(returnValue(myRoot));
      allowing(myContext).getBuild(); will(returnValue(myBuild));
      allowing(myContext).addEnvironmentVariable(with(any(String.class)), with(any(String.class)));
      will(new CustomAction("Add env parameter") {
        public Object invoke(Invocation invocation) {
          envParameters.put((String)invocation.getParameter(0), (String)invocation.getParameter(1));
          return null;
        }
      });

      allowing(myBuildParametersMap).getEnvironmentVariables(); will(returnValue(envParameters));
      allowing(myBuild).getBuildLogger();  will(returnValue(myLogger));
      allowing(myBuild).getCheckoutDirectory();  will(returnValue(myRoot));
      allowing(myBuild).getAgentTempDirectory(); will(returnValue(createTempDir()));
      allowing(myBuild).getBuildTempDirectory(); will(returnValue(createTempDir()));

      allowing(myMockProcess).start();
      allowing(myMockProcess).waitFor();
      will(returnValue(BuildFinishedStatus.FINISHED_SUCCESS));

      allowing(myLogger).message(with(any(String.class)));
      will(new CustomAction("Log message") {
        public Object invoke(Invocation invocation) {
          System.out.println((String)invocation.getParameter(0));
          return null;
        }
      });
      allowing(myLogger).warning(with(any(String.class)));
      will(new CustomAction("Log warning") {
        public Object invoke(Invocation invocation) {
          System.out.println((String)invocation.getParameter(0));
          return null;
        }
      });
      allowing(myLogger).error(with(any(String.class)));
      will(new CustomAction("Log error") {
        public Object invoke(Invocation invocation) {
          System.err.println((String)invocation.getParameter(0));
          return null;
        }
      });
      allowing(myLogger).getFlowId();
      will(returnValue("123"));
      allowing(myLogger).getFlowLogger(with(any(String.class)));
      will(returnValue(myLogger));

      allowing(myBuild).getBuildId(); will(returnValue(42L));
      allowing(myBuild).getSharedConfigParameters(); will(returnValue(Collections.unmodifiableMap(configParameters)));
      allowing(myBuild).addSharedConfigParameter(with(any(String.class)), with(any(String.class)));
      will(new CustomAction("Add config parameter") {
        public Object invoke(Invocation invocation) {
          configParameters.put((String)invocation.getParameter(0), (String)invocation.getParameter(1));
          return null;
        }
      });

      allowing(myPsm).getGlobalPackageSources(myBuild); will(returnValue(Collections.unmodifiableSet(myGlobalSources)));

      allowing(myExtensionHolder).getExtensions(with(Expectations.<Class<AgentExtension>>anything())); will(returnValue(Collections.emptyList()));
    }});

    BuildAgentConfiguration configuration = m.mock(BuildAgentConfiguration.class);
    m.checking(new Expectations() {{
      allowing(configuration).getServerUrl();
      will(returnValue("http://localhost:8080"));
    }});

    myCollector = new NuGetPackagesCollectorImpl(configuration);
    PackageUsages pu = new PackageUsagesImpl(
            myCollector,
            new NuGetPackagesConfigParser(),
            new PackageInfoLoader()
    );

    myExecutor = executingFactory();
    myActionFactory = new LoggingNuGetActionFactoryImpl(
      new NuGetActionFactoryImpl(
        new NuGetCommandBuildProcessFactory(
          myExtensionHolder,
          new NuGetCommandLineProvider(myNuGetTeamCityProvider, new CommandLineExecutorImpl())
        ),
        pu,
        new CommandFactoryImpl()
      )
    );

    // Add an access to run NuGet.exe
    for (NuGet nuGetVersion : NuGet.values()) {
      final String nuGet = nuGetVersion.getPath().getPath();
      enableExecution(nuGet, null);
    }

    try {
      arrangeNuGetPackageSource("update");
      return;
    } catch (Exception e) {
      if (!e.getMessage().startsWith("Unable to find any package source(s) matching name")){
        throw e;
      }
    }

    arrangeNuGetPackageSource("add");
  }

  private void arrangeNuGetPackageSource(final String command) throws Exception {
    final String nugetPath = NuGet.NuGet_2_8.getPath().getPath();
    enableExecution(nugetPath, null);

    final GeneralCommandLine cmd = new GeneralCommandLine();
    cmd.setExePath(nugetPath);
    cmd.addParameter("sources");
    cmd.addParameter(command);
    cmd.addParameter("-Name");
    cmd.addParameter("nuget.org");
    cmd.addParameter("-Source");
    cmd.addParameter("https://www.nuget.org/api/v2");

    Process process = null;
    try {
      process = cmd.createProcess();
      assertTrue("Failed to wait for command to finish " + cmd.getCommandLineString(), process.waitFor(5, TimeUnit.SECONDS));
    } catch (Exception e) {
      if (process != null) process.destroy();
      throw e;
    }

    if (process.exitValue() != 0) throw new Exception(StreamUtil.readText(process.getErrorStream()));
  }

  protected void addGlobalSource(@NotNull final String feed,
                                 @Nullable final String user,
                                 @Nullable final String pass) {
    myGlobalSources.add(new PackageSource() {
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
    });
  }

  @NotNull
  protected File getTestDataPath(final String path) {
    return Paths.getTestDataPath("integration/" + path);
  }

  @NotNull
  private CommandlineBuildProcessFactory executingFactory() {
    return (hostContext, program, argz, workingDir, additionalEnvironment) -> new BuildProcessBase() {
      @NotNull
      @Override
      protected BuildFinishedStatus waitForImpl() {
        if (!SystemInfo.isWindows) {
          enableExecution(program, workingDir.getAbsolutePath());
        }

        GeneralCommandLine cmd = new GeneralCommandLine();
        cmd.setExePath(program);
        for (String arg : argz) {
          cmd.addParameter(arg.replaceAll("%+", "%"));
        }
        cmd.setWorkingDirectory(workingDir);

        Map<String, String> env = new HashMap<>();
        env.putAll(System.getenv());
        env.putAll(additionalEnvironment);
        cmd.setEnvParams(env);

        System.out.println("Run: " + cmd.getCommandLineString());

        ExecResult result = SimpleCommandLineProcessRunner.runCommand(cmd, new byte[0]);

        System.out.println(result.getStdout());
        System.out.println(result.getStderr());

        myCommandsOutput.append(result.getStdout()).append("\n\n").append(result.getStderr()).append("\n\n");

        return result.getExitCode() == 0
                ? BuildFinishedStatus.FINISHED_SUCCESS
                : BuildFinishedStatus.FINISHED_FAILED;
      }
    };
  }

  protected static void enableExecution(@NotNull final String filePath, @Nullable final String baseDir) {
    if(SystemInfo.isWindows) {
      return;
    }

    final GeneralCommandLine commandLine = new GeneralCommandLine();

    String canonicalFilePath;
    try {
      canonicalFilePath = new File(filePath).getCanonicalPath();
    } catch (IOException e) {
      canonicalFilePath = filePath;
    }
    commandLine.setExePath("chmod");
    commandLine.addParameter("+x");
    commandLine.addParameter(canonicalFilePath);
    if (baseDir != null) {
      commandLine.setWorkDirectory(baseDir);
    }

    final ExecResult execResult = SimpleCommandLineProcessRunner.runCommand(commandLine, null);
    if (execResult.getExitCode() != 0) {
      System.out.println("Failed to set executable attribute for " + canonicalFilePath + ": chmod +x exit code is " + execResult.getExitCode());
    }
  }
}
