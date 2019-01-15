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

package jetbrains.buildServer.nuget.tests.integration.agent;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.impl.AgentEventDispatcher;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesCollectorImpl;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.runner.credentials.NuGetCredentialsPathProvider;
import jetbrains.buildServer.nuget.agent.runner.credentials.NuGetCredentialsProvider;
import jetbrains.buildServer.nuget.agent.runner.install.PackagesInstallerRunner;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolverImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.LocateNuGetConfigProcessFactory;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.ResourcesConfigPackagesScanner;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.SolutionPackagesScanner;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.SolutionWidePackagesConfigScanner;
import jetbrains.buildServer.nuget.agent.util.sln.impl.SolutionParserImpl;
import jetbrains.buildServer.nuget.common.NuGetPackageInfo;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.common.PackagesInstallMode;
import jetbrains.buildServer.nuget.common.PackagesUpdateMode;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 16:47
 */
public class InstallPackageIntegrationTestCase extends IntegrationTestBase {
  protected PackagesInstallParameters myInstallParameters;
  protected PackagesUpdateParameters myUpdateParameters;
  protected PackagesInstallMode myInstallMode;
  protected List<String> myCommandLineArguments;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myInstallParameters = m.mock(PackagesInstallParameters.class);
    myUpdateParameters = m.mock(PackagesUpdateParameters.class);

    myInstallMode = PackagesInstallMode.VIA_INSTALL;
    myCommandLineArguments = new ArrayList<>();

    m.checking(new Expectations(){{
      allowing(myInstallParameters).getNuGetParameters();
      will(returnValue(myFetchParameters));
      allowing(myUpdateParameters).getNuGetParameters();
      will(returnValue(myFetchParameters));
      allowing(myUpdateParameters).getCustomCommandline();
      will(returnValue(Collections.emptyList()));

      allowing(myInstallParameters).getInstallMode(); will(new CustomAction("return myInstallMode") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myInstallMode;
        }
      });

      allowing(myLogger).activityStarted(with(equal("install")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("install")), with(any(String.class)));
      allowing(myLogger).activityStarted(with(equal("restore")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("restore")), with(any(String.class)));
      allowing(myLogger).activityStarted(with(equal("scan")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("scan")), with(any(String.class)));
    }});
  }

  protected void fetchPackages(final File sln,
                             final List<String> sources,
                             final boolean excludeVersion,
                             final boolean noCache,
                             final boolean update,
                             @NotNull final NuGet nuget,
                             @Nullable Collection<NuGetPackageInfo> detectedPackages) throws RunBuildException {
    fetchPackages(sln, sources, excludeVersion, noCache, update, nuget, detectedPackages, BuildFinishedStatus.FINISHED_SUCCESS);
  }

  protected void fetchPackages(final File sln,
                             final List<String> sources,
                             final boolean excludeVersion,
                             final boolean noCache,
                             final boolean update,
                             @NotNull final NuGet nuget,
                             @Nullable Collection<NuGetPackageInfo> detectedPackages,
                             @Nullable BuildFinishedStatus status) throws RunBuildException {

    if (!SystemInfo.isWindows && nuget == NuGet.NuGet_4_8) {
      myCommandLineArguments.addAll(Arrays.asList("-Verbosity", "detailed"));
    }

    m.checking(new Expectations() {{
      allowing(myParametersFactory).loadNuGetFetchParameters(myContext);
      will(returnValue(myFetchParameters));
      allowing(myParametersFactory).loadInstallPackagesParameters(myContext, myFetchParameters);
      will(returnValue(myInstallParameters));

      allowing(myFetchParameters).getNuGetExeFile();
      will(returnValue(nuget.getPath()));
      allowing(myFetchParameters).getSolutionFile();
      will(returnValue(sln));
      allowing(myFetchParameters).getWorkingDirectory();
      will(returnValue(myRoot));
      allowing(myFetchParameters).getNuGetPackageSources();
      will(returnValue(sources));
      allowing(myFetchParameters).getCustomCommandline();
      will(returnValue(myCommandLineArguments));
      allowing(myInstallParameters).getExcludeVersion();
      will(returnValue(excludeVersion));
      allowing(myInstallParameters).getNoCache();
      will(returnValue(noCache));
      allowing(myParametersFactory).loadUpdatePackagesParameters(myContext, myFetchParameters);
      will(returnValue(update ? myUpdateParameters : null));
      allowing(myContext).getRunType();
      will(returnValue(PackagesConstants.INSTALL_RUN_TYPE));
    }});

    AgentEventDispatcher eventDispatcher = new AgentEventDispatcher();
    final NuGetCredentialsProvider provider = new NuGetCredentialsProvider(
      eventDispatcher, myPsm, Collections.singletonList(new NuGetCredentialsPathProvider(myNuGetTeamCityProvider))
    );
    try {
      eventDispatcher.getMulticaster().beforeRunnerStart(myContext);
      BuildProcess proc = new PackagesInstallerRunner(
        myActionFactory,
        myParametersFactory,
        new LocateNuGetConfigProcessFactory(
          new RepositoryPathResolverImpl(),
          Arrays.asList(
            new ResourcesConfigPackagesScanner(),
            new SolutionPackagesScanner(new SolutionParserImpl()),
            new SolutionWidePackagesConfigScanner())
        )
      ).createBuildProcess(myBuild, myContext);
      ((NuGetPackagesCollectorImpl)myCollector).removeAllPackages();

      assertRunSuccessfully(proc, status);
    } finally {
      eventDispatcher.getMulticaster().runnerFinished(myContext, BuildFinishedStatus.FINISHED_SUCCESS);
    }

    System.out.println(myCollector.getPackages());
    if (detectedPackages != null) {
      Assert.assertEquals(
        new TreeSet<>(myCollector.getPackages().getUsedPackages()),
        new TreeSet<>(detectedPackages)
      );
    }

    m.assertIsSatisfied();
  }

  protected void allowUpdate(final PackagesUpdateMode updateMode){
    allowUpdate(updateMode, false, false);
  }

  protected void allowUpdate(final PackagesUpdateMode updateMode, final boolean icludePrereleasePackages, final boolean useSafeUpdate) {
    m.checking(new Expectations() {{
      allowing(myLogger).activityStarted(with(equal("update")), with(any(String.class)), with(equal("nuget")));
      allowing(myLogger).activityFinished(with(equal("update")), with(equal("nuget")));

      allowing(myUpdateParameters).getUseSafeUpdate(); will(returnValue(useSafeUpdate));
      allowing(myUpdateParameters).getIncludePrereleasePackages(); will(returnValue(icludePrereleasePackages));
      allowing(myUpdateParameters).getPackagesToUpdate(); will(returnValue(Collections.<String>emptyList()));
      allowing(myUpdateParameters).getUpdateMode(); will(returnValue(updateMode));
    }});
  }
}
