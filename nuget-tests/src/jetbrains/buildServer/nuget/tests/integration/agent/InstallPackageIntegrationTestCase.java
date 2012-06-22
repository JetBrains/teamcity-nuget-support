/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.nuget.agent.dependencies.impl.NuGetPackagesCollectorImpl;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.parameters.PackagesInstallParameters;
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters;
import jetbrains.buildServer.nuget.agent.runner.install.PackagesInstallerRunner;
import jetbrains.buildServer.nuget.agent.runner.install.impl.RepositoryPathResolverImpl;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.LocateNuGetConfigProcessFactory;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.ResourcesConfigPackagesScanner;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.SolutionPackagesScanner;
import jetbrains.buildServer.nuget.agent.runner.install.impl.locate.SolutionWidePackagesConfigScanner;
import jetbrains.buildServer.nuget.agent.util.sln.impl.SolutionParserImpl;
import jetbrains.buildServer.nuget.common.PackageInfo;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Expectations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 16:47
 */
public class InstallPackageIntegrationTestCase extends IntegrationTestBase {
  protected PackagesInstallParameters myInstall;
  protected PackagesUpdateParameters myUpdate;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myInstall = m.mock(PackagesInstallParameters.class);
    myUpdate = m.mock(PackagesUpdateParameters.class);

    m.checking(new Expectations(){{
      allowing(myInstall).getNuGetParameters();
      will(returnValue(myNuGet));
      allowing(myUpdate).getNuGetParameters();
      will(returnValue(myNuGet));

      allowing(myLogger).activityStarted(with(equal("install")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("install")), with(any(String.class)));
      allowing(myLogger).activityStarted(with(equal("scan")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("scan")), with(any(String.class)));
      allowing(myLogger).activityStarted(with(equal("version")), with(any(String.class)), with(any(String.class)));
      allowing(myLogger).activityFinished(with(equal("version")), with(any(String.class)));
    }});
  }

  protected void fetchPackages(final File sln,
                             final List<PackageSource> sources,
                             final boolean excludeVersion,
                             final boolean update,
                             @NotNull final NuGet nuget,
                             @Nullable Collection<PackageInfo> detectedPackages) throws RunBuildException {
    fetchPackages(sln, sources, excludeVersion, update, nuget, detectedPackages, BuildFinishedStatus.FINISHED_SUCCESS);
  }
  protected void fetchPackages(final File sln,
                             final List<PackageSource> sources,
                             final boolean excludeVersion,
                             final boolean update,
                             @NotNull final NuGet nuget,
                             @Nullable Collection<PackageInfo> detectedPackages,
                             @Nullable BuildFinishedStatus status) throws RunBuildException {

    m.checking(new Expectations() {{
      allowing(myParametersFactory).loadNuGetFetchParameters(myContext);
      will(returnValue(myNuGet));
      allowing(myParametersFactory).loadInstallPackagesParameters(myContext, myNuGet);
      will(returnValue(myInstall));

      allowing(myNuGet).getNuGetExeFile();
      will(returnValue(nuget.getPath()));
      allowing(myNuGet).getSolutionFile();
      will(returnValue(sln));
      allowing(myNuGet).getNuGetPackageSources();
      will(returnValue(sources));
      allowing(myInstall).getExcludeVersion();
      will(returnValue(excludeVersion));
      allowing(myParametersFactory).loadUpdatePackagesParameters(myContext, myNuGet);
      will(returnValue(update ? myUpdate : null));
    }});

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

    System.out.println(myCollector.getUsedPackages());
    if (detectedPackages != null) {
      Assert.assertEquals(
              new TreeSet<PackageInfo>(myCollector.getUsedPackages().getUsedPackages()),
              new TreeSet<PackageInfo>(detectedPackages));
    }

    m.assertIsSatisfied();
  }



}
