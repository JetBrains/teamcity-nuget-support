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
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.ExecResult;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.SimpleCommandLineProcessRunner;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.dotNet.DotNetConstants;
import jetbrains.buildServer.nuget.agent.commands.NuGetActionFactory;
import jetbrains.buildServer.nuget.agent.commands.impl.*;
import jetbrains.buildServer.nuget.agent.dependencies.NuGetPackagesCollector;
import jetbrains.buildServer.nuget.agent.dependencies.PackageUsages;
import jetbrains.buildServer.nuget.agent.dependencies.impl.*;
import jetbrains.buildServer.nuget.agent.parameters.*;
import jetbrains.buildServer.nuget.agent.util.impl.CommandLineExecutorImpl;
import jetbrains.buildServer.nuget.agent.util.impl.NuGetCommandBuildProcessFactory;
import jetbrains.buildServer.nuget.agent.util.impl.NuGetCommandLineProvider;
import jetbrains.buildServer.nuget.agent.util.impl.SystemInformationImpl;
import jetbrains.buildServer.nuget.common.PackageInfoLoader;
import jetbrains.buildServer.nuget.common.auth.PackageSource;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider;
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProviderBase;
import jetbrains.buildServer.nuget.tests.util.BuildProcessTestCase;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
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

import static com.intellij.openapi.util.SystemInfo.isWindows;
import static java.lang.System.*;
import static java.util.Collections.*;
import static jetbrains.buildServer.agent.BuildFinishedStatus.FINISHED_SUCCESS;
import static jetbrains.buildServer.dotNet.DotNetConstants.MONO_JIT;
import static jetbrains.buildServer.nuget.tests.integration.NuGet.values;
import static jetbrains.buildServer.nuget.tests.integration.Paths.getCredentialProviderHomeDirectory;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 22.07.11 1:26
 */
public class IntegrationTestBase extends BuildProcessTestCase {
  private StringBuilder myCommandsOutput;
  private StringBuilder myCommandsWarnings;
  protected File myRoot;
  protected Mockery m;
  protected AgentRunningBuild myBuild;
  protected BuildRunnerContext myContext;
  protected FlowLogger myLogger;
  protected PackagesParametersFactory myParametersFactory;
  protected NuGetFetchParameters myFetchParameters;
  protected NuGetPackagesCollector myCollector;
  protected NuGetActionFactory myActionFactory;
  private BuildProcess myMockProcess;
  private BuildParametersMap myBuildParametersMap;
  protected NuGetTeamCityProvider myNuGetTeamCityProvider;
  private Set<PackageSource> myGlobalSources;
  private ExtensionHolder myExtensionHolder;
  protected PackageSourceManager myPsm;

  @NotNull
  protected String getCommandsOutput() {
    return myCommandsOutput.toString();
  }

  @NotNull
  protected String getCommandsWarnings() {
    return myCommandsWarnings.toString();
  }

  protected static final String NUGET_VERSIONS = "nuget_versions";
  protected static final String NUGET_VERSIONS_15p = "nuget_versions_15p";
  protected static final String NUGET_VERSIONS_16p = "nuget_versions_16p";
  protected static final String NUGET_VERSIONS_17p = "nuget_versions_17p";
  protected static final String NUGET_VERSIONS_18p = "nuget_versions_18p";
  protected static final String NUGET_VERSIONS_20p = "nuget_versions_20p";
  protected static final String NUGET_VERSIONS_27p = "nuget_versions_27p";
  protected static final String NUGET_VERSIONS_28p = "nuget_versions_28p";
  protected static final String NUGET_VERSIONS_48p = "nuget_versions_48p";
  private static final NuGet MIN_TESTABLE_VERSION = NuGet.NuGet_3_5;

  @NotNull
  protected Object[][] versionsFrom(@NotNull final NuGet lowerBound) {
    final NuGet[] values =  NuGet.values();
    final List<Object[]> data = new ArrayList<>();
    for (NuGet value : values) {
      if (value.version.compareTo(lowerBound.version) < 0) {
        continue;
      }

      if(!SystemInfo.isWindows) {
        if(value.version.compareTo(NuGet.NuGet_3_3.version) < 0) {
          continue;
        }
      }

      if (value.version.compareTo(MIN_TESTABLE_VERSION.version) < 0) {
        continue;
      }

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

  @DataProvider(name = NUGET_VERSIONS_48p)
  public Object[][] dataProviderNuGetVersions48p() {
    return versionsFrom(NuGet.NuGet_4_8);
  }

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myGlobalSources = new HashSet<>();
    myCommandsOutput = new StringBuilder();
    myCommandsWarnings = new StringBuilder();
    myRoot = createTempDir();
    m = TCJMockUtils.createInstance();
    myBuild = m.mock(AgentRunningBuild.class);
    myContext = m.mock(BuildRunnerContext.class);
    myLogger = m.mock(FlowLogger.class);
    myParametersFactory = m.mock(PackagesParametersFactory.class);
    myMockProcess = m.mock(BuildProcess.class);
    myFetchParameters = m.mock(NuGetFetchParameters.class);
    myBuildParametersMap = m.mock(BuildParametersMap.class);
    File extensionsPath = getCredentialProviderHomeDirectory().getParentFile().getParentFile();
    myNuGetTeamCityProvider = new NuGetTeamCityProviderBase(extensionsPath);
    myExtensionHolder = m.mock(ExtensionHolder.class);

    myPsm = m.mock(PackageSourceManager.class);

    final Map<String, String> configParameters = new TreeMap<>();
    if (!isWindows) {
      configParameters.put(MONO_JIT, "/usr/bin/mono-sgen");
    }
    final Map<String, String> envParameters = new TreeMap<>(getenv());

    m.checking(new Expectations(){{
      allowing(myContext).getBuildParameters(); will(returnValue(myBuildParametersMap));
      allowing(myContext).getConfigParameters(); will(returnValue(configParameters));
      allowing(myContext).getWorkingDirectory(); will(returnValue(myRoot));
      allowing(myContext).getBuild(); will(returnValue(myBuild));
      allowing(myContext).getId(); will(returnValue("id"));
      allowing(myContext).getName(); will(returnValue("name"));
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
      will(returnValue(FINISHED_SUCCESS));

      allowing(myLogger).getFlowLogger(with(any(String.class)));
      will(returnValue(myLogger));
      allowing(myLogger).startFlow();
      allowing(myLogger).disposeFlow();
      allowing(myLogger).message(with(any(String.class)));
      will(new CustomAction("Log message") {
        public Object invoke(Invocation invocation) {
          String message = (String) invocation.getParameter(0);
          out.println(message);
          myCommandsOutput.append(message).append("\n");
          return null;
        }
      });
      allowing(myLogger).warning(with(any(String.class)));
      will(new CustomAction("Log warning") {
        public Object invoke(Invocation invocation) {
          String message = (String) invocation.getParameter(0);
          out.println(message);
          myCommandsWarnings.append(message).append("\n");
          return null;
        }
      });
      allowing(myLogger).error(with(any(String.class)));
      will(new CustomAction("Log error") {
        public Object invoke(Invocation invocation) {
          err.println((String)invocation.getParameter(0));
          return null;
        }
      });
      allowing(myLogger).getFlowId();
      will(returnValue("123"));
      allowing(myLogger).getFlowLogger(with(any(String.class)));
      will(returnValue(myLogger));
      allowing(myLogger).logBuildProblem(with(any(BuildProblemData.class)));

      allowing(myBuild).getBuildId(); will(returnValue(42L));
      allowing(myBuild).getSharedConfigParameters(); will(returnValue(unmodifiableMap(configParameters)));
      allowing(myBuild).addSharedConfigParameter(with(any(String.class)), with(any(String.class)));
      will(new CustomAction("Add config parameter") {
        public Object invoke(Invocation invocation) {
          configParameters.put((String)invocation.getParameter(0), (String)invocation.getParameter(1));
          return null;
        }
      });

      allowing(myPsm).getGlobalPackageSources(myBuild); will(returnValue(unmodifiableSet(myGlobalSources)));

      allowing(myExtensionHolder).getExtensions(with(Expectations.<Class<AgentExtension>>anything())); will(returnValue(emptyList()));
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

    myActionFactory = new LoggingNuGetActionFactoryImpl(
      new NuGetActionFactoryImpl(
        new NuGetCommandBuildProcessFactory(
          myExtensionHolder,
          new NuGetCommandLineProvider(
            myNuGetTeamCityProvider,
            new CommandLineExecutorImpl(),
            new SystemInformationImpl()
          )
        ),
        pu,
        new CommandFactoryImpl()
      )
    );

    // Add an access to run NuGet.exe
    for (NuGet nuGetVersion : values()) {
      final String nuGet = nuGetVersion.getPath().getPath();
      enableExecution(nuGet);
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
    enableExecution(nugetPath);

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

  protected void clearGlobalSources() {
    myGlobalSources.clear();
  }

  @NotNull
  protected File getTestDataPath(final String path) {
    return Paths.getTestDataPath("integration/" + path);
  }

  static void enableExecution(@NotNull final String filePath) {
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

    final ExecResult execResult = SimpleCommandLineProcessRunner.runCommand(commandLine, null);
    if (execResult.getExitCode() != 0) {
      System.out.println("Failed to set executable attribute for " + canonicalFilePath + ": chmod +x exit code is " + execResult.getExitCode());
    }
  }
}
