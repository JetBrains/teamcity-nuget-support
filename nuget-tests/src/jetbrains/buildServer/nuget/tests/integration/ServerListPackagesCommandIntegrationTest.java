/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

import jetbrains.buildServer.TempFolderProvider;
import jetbrains.buildServer.nuget.server.exec.*;
import jetbrains.buildServer.nuget.server.exec.impl.ListPackagesCommandImpl;
import jetbrains.buildServer.nuget.server.exec.impl.NuGetExecutorImpl;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 21:24
 */
public class ServerListPackagesCommandIntegrationTest extends IntegrationTestBase {
  private ListPackagesCommand myCommand;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    Mockery m = new Mockery();
    final SystemInfo info = m.mock(SystemInfo.class);
    final NuGetTeamCityProvider prov = m.mock(NuGetTeamCityProvider.class);
    final TempFolderProvider temp = m.mock(TempFolderProvider.class);

    m.checking(new Expectations(){{
      allowing(info).canStartNuGetProcesses(); will(returnValue(true));


      allowing(prov).getNuGetRunnerPath(); will(returnValue(Paths.getNuGetRunnerPath()));
      allowing(temp).getTempDirectory(); will(returnValue(createTempDir()));
    }});

    myCommand = new ListPackagesCommandImpl(new NuGetExecutorImpl(prov, info), temp);
  }

  @Test(dataProvider = NUGET_VERSIONS)
  public void test_batch_reportNUnitAndYouTrackSharp_from_default_feed(@NotNull final NuGet nuget) throws NuGetExecutionException {
    final SourcePackageReference nunit_all = new SourcePackageReference(null, "NUnit", null);
    final SourcePackageReference nunit_filter = new SourcePackageReference(null, "NUnit", "(1.1.1.1, 2.5.9.1)");
    final SourcePackageReference youTrackSharp = new SourcePackageReference(null, "YouTrackSharp", null);
    final Map<SourcePackageReference,Collection<SourcePackageInfo>> m1 = myCommand.checkForChanges(
            nuget.getPath(),
            Arrays.asList(
                    nunit_all,
                    nunit_filter,
                    youTrackSharp
            ));

    Assert.assertTrue(m1.size() == 3);
    System.out.println("m = " + m1);

    for (Collection<SourcePackageInfo> infos : m1.values()) {
      Assert.assertTrue(infos.size() > 0);
    }

    final Collection<SourcePackageInfo> nAll = m1.get(nunit_all);
    final Collection<SourcePackageInfo> nFilter = m1.get(nunit_filter);

    Assert.assertTrue(nAll.size() > nFilter.size());
  }
}
